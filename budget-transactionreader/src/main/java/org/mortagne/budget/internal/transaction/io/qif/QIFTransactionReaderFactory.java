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

import java.io.InputStream;

import org.mortagne.budget.transaction.io.AbstractTransactionReaderFactory;
import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.xwiki.component.annotation.Component;

@Component("qif")
public class QIFTransactionReaderFactory extends AbstractTransactionReaderFactory
{
    public QIFTransactionReaderFactory()
    {
        super("qif", "Generic QIF", "Generic QIF");
    }

    public TransactionReader createTransactionReader(InputStream transationStream,
        TransactionReaderConfiguration configuration)
    {
        QIFTransactionReader reader = new QIFTransactionReader(transationStream, configuration);

        reader.enableLogging(getLogger());

        return reader;
    }
}
