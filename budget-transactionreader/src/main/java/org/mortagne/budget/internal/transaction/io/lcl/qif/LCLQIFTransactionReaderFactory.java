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

import java.io.InputStream;
import java.text.DateFormat;
import java.util.Locale;

import org.mortagne.budget.transaction.io.AbstractTransactionReaderFactory;
import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.xwiki.component.annotation.Component;

@Component("lcl.qif")
public class LCLQIFTransactionReaderFactory extends AbstractTransactionReaderFactory
{
    public LCLQIFTransactionReaderFactory()
    {
        super("lcl.qif", "LCL QIF", "LCL French QIF format");
    }

    public TransactionReader createTransactionReader(InputStream transationStream,
        TransactionReaderConfiguration configuration)
    {
        configuration.setDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE));
        configuration.setCharset("ISO-8859-1");

        return new LCLQIFTransactionReader(transationStream, configuration);
    }
}
