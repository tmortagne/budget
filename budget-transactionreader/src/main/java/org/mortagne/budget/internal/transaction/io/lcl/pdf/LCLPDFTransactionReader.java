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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Iterator;

import org.mortagne.budget.transaction.DefaultTransaction;
import org.mortagne.budget.transaction.Transaction;
import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.xwiki.component.logging.AbstractLogEnabled;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

public class LCLPDFTransactionReader extends AbstractLogEnabled implements TransactionReader
{
    private InputStream transationStream;

    private TransactionReaderConfiguration configuration;

    private Iterator<DefaultTransaction> it = null;

    public LCLPDFTransactionReader(InputStream transationStream, TransactionReaderConfiguration configuration)
    {
        if (transationStream == null) {
            throw new RuntimeException("transationStream argument cannot be null");
        }

        this.transationStream = transationStream;
        this.configuration = configuration;
    }

    public TransactionReaderConfiguration getConfiguration()
    {
        return this.configuration;
    }

    public void open() throws IOException
    {
        close();

        // IText
        LCLLocationTextExtractionStrategy textExtractionStrategy = new LCLLocationTextExtractionStrategy();
        PdfReader reader = new PdfReader(this.transationStream);

        try {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            parser.processContent(3, textExtractionStrategy);
        } finally {
            reader.close();
        }

        this.it = textExtractionStrategy.getTransactions().iterator();
    }

    public void close() throws IOException
    {
        this.it = null;
    }

    public Transaction next() throws IOException, ParseException
    {
        if (this.it == null) {
            open();
        }

        if (!hasMore()) {
            return null;
        }

        return this.it.next();
    }

    public boolean hasMore()
    {
        return this.it != null && this.it.hasNext();
    }
}
