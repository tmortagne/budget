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
package org.mortagne.budget.internal.transaction.io.lcl.qif;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.mortagne.budget.internal.transaction.io.qif.QIFTransactionReader;
import org.mortagne.budget.transaction.DefaultTransaction;
import org.mortagne.budget.transaction.Transaction;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;

public class LCLQIFTransactionReader extends QIFTransactionReader
{
    public LCLQIFTransactionReader(InputStream transationStream, TransactionReaderConfiguration configuration)
    {
        super(transationStream, configuration);
    }

    @Override
    public Transaction next() throws IOException, ParseException
    {
        DefaultTransaction transaction = (DefaultTransaction) super.next();

        if (transaction != null) {
            if (transaction.getDescription() != null) {
                // trim the description
                transaction.setDescription(transaction.getDescription().trim());

                // extract real date from the description
                if (transaction.getType().equals("Carte")) {
                    String realDateValue = StringUtils.substringAfterLast(transaction.getDescription(), " ");

                    try {
                        transaction.setRealDate(getConfiguration().getDateFormat().parse(realDateValue));
                        transaction.setDescription(transaction.getDescription()
                            .substring(0, transaction.getDescription().length() - realDateValue.length()).trim());
                    } catch (ParseException e) {
                        // TODO: log
                    }
                }
            }
        }

        return transaction;
    }
}
