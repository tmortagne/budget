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
package org.mortagne.budget.internal.transaction.io.qif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.mortagne.budget.transaction.DefaultTransaction;
import org.mortagne.budget.transaction.Transaction;
import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.xwiki.component.logging.AbstractLogEnabled;

public class QIFTransactionReader extends AbstractLogEnabled implements TransactionReader
{
    private InputStream transationStream;

    private BufferedReader buffer;

    private String type;

    private TransactionReaderConfiguration configuration;

    public QIFTransactionReader(InputStream transationStream, TransactionReaderConfiguration configuration)
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

    public String getType()
    {
        return type;
    }

    public void open() throws IOException
    {
        close();

        this.buffer = new BufferedReader(new InputStreamReader(this.transationStream, this.configuration.getCharset()));

        this.type = this.buffer.readLine();
    }

    public void close() throws IOException
    {
        if (this.buffer != null) {
            this.buffer.close();
        }

        this.type = null;
    }

    public Transaction next() throws IOException, ParseException
    {
        if (this.buffer == null) {
            open();
        }

        if (!hasMore()) {
            return null;
        }

        DefaultTransaction transaction = new DefaultTransaction();

        for (String line = this.buffer.readLine(); line != null && line.charAt(0) != '^'; line = this.buffer.readLine()) {
            char type = line.charAt(0);

            String value = line.substring(1, line.length());

            switch (type) {
                case 'D':
                    if (this.configuration.getDateFormat() != null) {
                        transaction.setDate(this.configuration.getDateFormat().parse(value));
                    }
                    // TODO: use a default DateFormat
                    break;
                case 'M':
                case 'P':
                    transaction.setDescription(value);
                    break;
                case 'N':
                    transaction.setType(value);
                    break;
                case 'T':
                    transaction.setValue(Double.valueOf(value));
                    break;
                default:
                    break;
            }
        }

        return transaction;
    }

    public boolean hasMore()
    {
        boolean result = false;

        try {
            result = this.buffer.ready();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
