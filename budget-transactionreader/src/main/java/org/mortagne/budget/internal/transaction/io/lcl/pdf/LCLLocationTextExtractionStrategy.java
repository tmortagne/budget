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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mortagne.budget.transaction.DefaultTransaction;

import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

public class LCLLocationTextExtractionStrategy implements TextExtractionStrategy
{
    private static final List<String> TABLEHEADER = Arrays.asList("DATE", "LIBELLE", "VALEUR", "DEBIT", "CREDIT");

    private static final DateFormat DATEFORMAT = new SimpleDateFormat("dd.MM.yy");

    /** a summary of all found text */
    private final List<TextChunk> locationalResult = new ArrayList<TextChunk>();

    private List<DefaultTransaction> transactions;

    /**
     * Creates a new text extraction renderer.
     */
    public LCLLocationTextExtractionStrategy()
    {
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
                    if (TABLEHEADER.get(tableheaderindex).equals(chunk.text)) {
                        ++tableheaderindex;
                        
                        if (tableheaderindex == TABLEHEADER.size()) {
                            inTable = true;
                            tableheaderindex = 0;
                        }
                    } else {
                        tableheaderindex = 0;
                    }
                } else if (chunk.text.equals("SOIT EN  FRANCS")) {
                    break;
                } else {
                    if (lastChunk == null) {
                        if (chunk.text.matches("\\d\\d\\.\\d\\d")) {
                            this.transactions.add(currentTransaction = new DefaultTransaction());
                            previousDate = chunk.text.trim();
                            tableheaderindex = 1;
                        } else {
                            // TODO: throw exception
                        }
                    } else {
                        if (chunk.sameLine(lastChunk)) {
                            if (tableheaderindex == 1) {
                                currentTransaction.setDescription(chunk.text.trim());
                                ++tableheaderindex;
                            } else if (tableheaderindex == 2) {
                                try {
                                    Date realDate = DATEFORMAT.parse(chunk.text.trim());
                                    currentTransaction.setRealDate(realDate);

                                    Date date;
                                    if ((realDate.getMonth() + 1) <= Integer.valueOf(StringUtils.substringAfterLast(
                                        previousDate, "."))) {
                                        date =
                                            DATEFORMAT.parse(previousDate
                                                + '.' + StringUtils.substringAfterLast(chunk.text, "."));
                                    } else {
                                        date =
                                            DATEFORMAT
                                                .parse(previousDate
                                                    + (Integer.valueOf(StringUtils.substringAfterLast(chunk.text, ".")) + 1));
                                    }
                                    currentTransaction.setDate(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                ++tableheaderindex;
                            } else if (tableheaderindex == 3) {
                                currentTransaction.setValue(Float.valueOf(chunk.text.trim().replace(',', '.').replace(" ", "")));

                                if (chunk.distParallelStart < 450f) {
                                    currentTransaction.setValue(currentTransaction.getValue() * -1);
                                }
                                tableheaderindex = 0;
                            }
                        } else {
                            if (chunk.distParallelStart > 500) {
                                break;
                            }

                            if (chunk.text.matches("\\d\\d\\.\\d\\d")) {
                                this.transactions.add(currentTransaction = new DefaultTransaction());
                                previousDate = chunk.text.trim();
                                tableheaderindex = 1;
                            } else if (this.transactions != null) {
                                currentTransaction.setDetails(chunk.text.trim());
                            }
                        }
                    }
                }

                lastChunk = chunk;
            }
        }

        return this.transactions;
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

        /** the starting location of the chunk */
        final Vector startLocation;

        /** the ending location of the chunk */
        final Vector endLocation;

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

        /**
         * distance of the end of the chunk parallel to the orientation unit vector (i.e. the X position in an unrotated
         * coordinate system)
         */
        final float distParallelEnd;

        /** the width of a single space character in the font of the chunk */
        final float charSpaceWidth;

        public TextChunk(String string, Vector startLocation, Vector endLocation, float charSpaceWidth)
        {
            this.text = string;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.charSpaceWidth = charSpaceWidth;

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
            this.distParallelEnd = this.orientationVector.dot(endLocation);
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
         * Computes the distance between the end of 'other' and the beginning of this chunk in the direction of this
         * chunk's orientation vector. Note that it's a bad idea to call this for chunks that aren't on the same line
         * and orientation, but we don't explicitly check for that condition for performance reasons.
         * 
         * @param other
         * @return the number of spaces between the end of 'other' and the beginning of this chunk
         */
        public float distanceFromEndOf(TextChunk other)
        {
            return this.distParallelStart - other.distParallelEnd;
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
