/*
 * Copyright (c) 2011, Thomas Mortagne. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mortagne.budget.internal.transaction.io.lcl.pdf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mortagne.budget.transaction.DefaultTransaction;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

public class LCLLocationTextExtractionStrategy implements TextExtractionStrategy
{
    private static final String[] TABLEHEADER = {"DATE", "LIBELLE", "VALEUR", "DEBIT", "CREDIT"};

    private static final float[] TABLECOLUMNS =
        {40/* 42.5 */, 70/* 74.85 */, 350/* 365.65 */, 410/* 421.05 */, 480/* 493.25 */};

    private static final float FTYPE = 85/* 89.85 */;

    private static final float FDETAIL = 80/* 80.85 */;

    private static final DateFormat DATEFORMAT = new SimpleDateFormat("dd.MM.yy");

    private static final DateFormat DESCRIPTIONDATEFORMAT = new SimpleDateFormat("dd/MM/yy");

    private static final Pattern DATEPATTERN = Pattern.compile("\\d\\d\\.\\d\\d");

    private static final Pattern DESCRIPTIONDATEPATTERN = Pattern.compile(".*(\\d\\d/\\d\\d/\\d\\d).*");

    private static final Pattern DESCRIPTIONDATEPATTERN2 = Pattern.compile(".*(\\d\\d/\\d\\d).*");

    /** a summary of all found text */
    private final List<TextChunk> locationalResult = new ArrayList<TextChunk>();

    private List<DefaultTransaction> transactions;

    private double previousTotal;

    private String lastType;

    public LCLLocationTextExtractionStrategy(String lastType)
    {
        this.lastType = lastType;
    }

    public double getPreviousTotal()
    {
        return this.previousTotal;
    }

    public String getLastType()
    {
        return lastType;
    }

    public List<DefaultTransaction> getTransactions()
    {
        if (this.transactions == null) {
            this.transactions = new ArrayList<DefaultTransaction>();

            Collections.sort(this.locationalResult);

            int tableheaderindex = 0;
            boolean inTable = false;

            DefaultTransaction currentTransaction = null;
            TextChunk lastChunk = null;
            String previousDate = null;
            for (TextChunk chunk : this.locationalResult) {
                if (!inTable) {
                    if (TABLEHEADER[tableheaderindex].equals(chunk.text)) {
                        ++tableheaderindex;

                        if (tableheaderindex == TABLEHEADER.length) {
                            inTable = true;
                            tableheaderindex = 0;
                        }
                    } else {
                        tableheaderindex = 0;
                    }
                } else if (chunk.text.equals("SOIT EN  FRANCS")) {
                    break;
                } else if (lastChunk != null && lastChunk.text.trim().equals("ANCIEN SOLDE")) {
                    this.previousTotal = parseValue(chunk.text);
                } else {
                    if (chunk.sameLine(lastChunk) && currentTransaction != null) {
                        int index = TABLECOLUMNS.length - 1;
                        for (; TABLECOLUMNS[index] > chunk.distParallelStart; --index)
                            ;

                        if (index == 1) {
                            String description = currentTransaction.getDescription();
                            currentTransaction.setDescription(description == null ? chunk.text.trim() : description
                                + " " + chunk.text.trim());
                            ++tableheaderindex;

                            Matcher matcher = DESCRIPTIONDATEPATTERN.matcher(chunk.text);
                            if (matcher.matches()) {
                                String realDate = matcher.group(1);

                                try {
                                    currentTransaction.setRealDate(DESCRIPTIONDATEFORMAT.parse(realDate));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (index == 2) {
                            try {
                                // Bank date
                                currentTransaction.setDate(DATEFORMAT.parse(chunk.text.trim()));

                                // Real date
                                if (currentTransaction.getRealDate() == null) {
                                    currentTransaction.setRealDate(getRealDate(currentTransaction.getDate(),
                                        previousDate, currentTransaction.getDescription()));
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            ++tableheaderindex;
                        } else if (index >= 3) {
                            if (!chunk.text.trim().equals(".")) {
                                double value = parseValue(chunk.text);

                                currentTransaction.setValue(value);
                                if (chunk.distParallelStart < TABLECOLUMNS[4]) {
                                    currentTransaction.setValue(currentTransaction.getValue() * -1);
                                }
                            }
                        }
                    } else {
                        if (chunk.distParallelStart > 500) {
                            break;
                        }

                        if (DATEPATTERN.matcher(chunk.text.trim()).matches()) {
                            this.transactions.add(currentTransaction = new DefaultTransaction());
                            currentTransaction.setType(this.lastType);
                            previousDate = chunk.text.trim();
                            tableheaderindex = 1;
                        } else {
                            if (chunk.distParallelStart > 100) {
                                // Do nothing
                            } else if (chunk.distParallelStart > FTYPE) {
                                this.lastType = chunk.text.trim();
                            } else if (chunk.distParallelStart > FDETAIL) {
                                String details = currentTransaction.getDetails();
                                currentTransaction.setDetails(details == null ? chunk.text.trim() : details + "\n"
                                    + chunk.text.trim());
                            }
                        }
                    }
                }

                lastChunk = chunk;
            }
        }

        return this.transactions;
    }

    private Date getRealDate(Date bankDate, String previousDate, String description) throws ParseException
    {
        int bankDateMonth = bankDate.getMonth() + 1;
        int bankDateYear = bankDate.getYear() + 1900;

        String bankDateMonthString = String.valueOf(bankDateMonth);

        Matcher matcher = DESCRIPTIONDATEPATTERN2.matcher(description);
        if (matcher.matches()) {
            String realDateString = matcher.group(1);
            return DESCRIPTIONDATEFORMAT.parse(realDateString + '/'
                + (realDateString.endsWith(bankDateMonthString) ? bankDateYear : (bankDateYear - 1)));
        } else {
            return DATEFORMAT.parse(previousDate + '.'
                + (previousDate.endsWith(bankDateMonthString) ? bankDateYear : (bankDateYear + 1)));
        }
    }

    private double parseValue(String value)
    {
        return Double.valueOf(value.trim().replace(',', '.').replace(" ", ""));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.itextpdf.text.pdf.parser.RenderListener#beginTextBlock()
     */
    public void beginTextBlock()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.itextpdf.text.pdf.parser.RenderListener#endTextBlock()
     */
    public void endTextBlock()
    {
    }

    /**
     * Returns the result so far.
     * 
     * @return a String with the resulting text.
     */
    public String getResultantText()
    {
        return "";
    }

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderText(com.itextpdf.text.pdf.parser.TextRenderInfo)
     */
    public void renderText(TextRenderInfo renderInfo)
    {
        LineSegment segment = renderInfo.getBaseline();
        TextChunk location =
            new TextChunk(renderInfo.getText(), segment.getStartPoint(), segment.getEndPoint(),
                renderInfo.getSingleSpaceWidth());
        this.locationalResult.add(location);
    }

    /**
     * Represents a chunk of text, it's orientation, and location relative to the orientation vector
     */
    private static class TextChunk implements Comparable<TextChunk>
    {
        /** the text of the chunk */
        final String text;

        /** unit vector in the orientation of the chunk */
        final Vector orientationVector;

        /** the orientation as a scalar for quick sorting */
        final int orientationMagnitude;

        /**
         * perpendicular distance to the orientation unit vector (i.e. the Y position in an unrotated coordinate system)
         * we round to the nearest integer to handle the fuzziness of comparing floats
         */
        final int distPerpendicular;

        /**
         * distance of the start of the chunk parallel to the orientation unit vector (i.e. the X position in an
         * unrotated coordinate system)
         */
        final float distParallelStart;

        public TextChunk(String string, Vector startLocation, Vector endLocation, float charSpaceWidth)
        {
            this.text = string;

            this.orientationVector = endLocation.subtract(startLocation).normalize();
            this.orientationMagnitude =
                (int) (Math.atan2(this.orientationVector.get(Vector.I2), this.orientationVector.get(Vector.I1)) * 1000);

            // see http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
            // the two vectors we are crossing are in the same plane, so the result will be purely
            // in the z-axis (out of plane) direction, so we just take the I3 component of the result
            Vector origin = new Vector(0, 0, 1);
            this.distPerpendicular =
                (int) (startLocation.subtract(origin)).cross(this.orientationVector).get(Vector.I3);

            this.distParallelStart = this.orientationVector.dot(startLocation);
        }

        /**
         * @param as the location to compare to
         * @return true is this location is on the the same line as the other
         */
        public boolean sameLine(TextChunk as)
        {
            if (this.orientationMagnitude != as.orientationMagnitude) {
                return false;
            }

            if (this.distPerpendicular != as.distPerpendicular) {
                return false;
            }

            return true;
        }

        /**
         * Compares based on orientation, perpendicular distance, then parallel distance
         * 
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TextChunk rhs)
        {
            if (this == rhs)
                return 0; // not really needed, but just in case

            int rslt;
            rslt = compareInts(this.orientationMagnitude, rhs.orientationMagnitude);
            if (rslt != 0)
                return rslt;

            rslt = compareInts(this.distPerpendicular, rhs.distPerpendicular);
            if (rslt != 0)
                return rslt;

            // note: it's never safe to check floating point numbers for equality, and if two chunks
            // are truly right on top of each other, which one comes first or second just doesn't matter
            // so we arbitrarily choose this way.
            rslt = this.distParallelStart < rhs.distParallelStart ? -1 : 1;

            return rslt;
        }

        /**
         * @param int1
         * @param int2
         * @return comparison of the two integers
         */
        private static int compareInts(int int1, int int2)
        {
            return int1 == int2 ? 0 : int1 < int2 ? -1 : 1;
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.itextpdf.text.pdf.parser.RenderListener#renderImage(com.itextpdf.text.pdf.parser.ImageRenderInfo)
     */
    public void renderImage(ImageRenderInfo renderInfo)
    {
        // do nothing
    }
}
