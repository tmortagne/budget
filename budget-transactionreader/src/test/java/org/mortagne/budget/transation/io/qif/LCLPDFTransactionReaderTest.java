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
package org.mortagne.budget.transation.io.qif;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mortagne.budget.transaction.Transaction;
import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.mortagne.budget.transaction.io.TransactionReaderFactory;
import org.xwiki.test.AbstractComponentTestCase;

public class LCLPDFTransactionReaderTest extends AbstractComponentTestCase
{
    private static final DateFormat DATEFORMAT = new SimpleDateFormat("dd.MM.yy");

    private TransactionReader reader;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        TransactionReaderConfiguration configuration = new TransactionReaderConfiguration();

        TransactionReaderFactory factory = getComponentManager().lookup(TransactionReaderFactory.class, "lcl.pdf");
        this.reader =
            factory.createTransactionReader(getClass().getResourceAsStream("/example.lcl.pdf"), configuration);
    }

    @Test
    public void testParse() throws Exception
    {
        Transaction transaction = this.reader.next();

        Assert.assertNotNull(transaction);

        Assert.assertEquals(-33.25d, transaction.getValue());
        Assert.assertEquals(11719.66d, transaction.getTotal());
        Assert.assertEquals(DATEFORMAT.parse("08.10.09"), transaction.getDate());
        Assert.assertEquals(DATEFORMAT.parse("07.10.09"), transaction.getRealDate());
        Assert.assertEquals("PAIEMENTS PAR CARTE", transaction.getType());
        Assert.assertEquals("CB BOUCHERIE LIMOUS 07/10/09", transaction.getDescription());
        Assert.assertNull(transaction.getDetails());

        transaction = this.reader.next();
        transaction = this.reader.next();
        transaction = this.reader.next();

        Assert.assertNotNull(transaction);

        Assert.assertEquals(-814.27d, transaction.getValue());
        Assert.assertEquals(10882.83d, transaction.getTotal());
        Assert.assertEquals(DATEFORMAT.parse("08.10.09"), transaction.getDate());
        Assert.assertEquals(DATEFORMAT.parse("08.10.09"), transaction.getRealDate());
        Assert.assertEquals("OPERATIONS DIVERSES", transaction.getType());
        Assert.assertEquals("PRLV ADOPT IMMO", transaction.getDescription());
        Assert.assertEquals("PL19171 PRELT", transaction.getDetails());

        for (transaction = reader.next(); transaction != null;) {
            transaction = reader.next();
        }

        this.reader.close();
    }
}
