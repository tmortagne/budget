package org.mortagne.budget.transation.io.qif;

import java.sql.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mortagne.budget.transation.Transaction;
import org.mortagne.budget.transation.io.TransactionReader;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;
import org.mortagne.budget.transation.io.TransactionReaderFactory;
import org.xwiki.test.AbstractComponentTestCase;

public class LCLQIFTransactionReaderTest extends AbstractComponentTestCase
{
    private TransactionReader reader;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        TransactionReaderConfiguration configuration = new TransactionReaderConfiguration();

        TransactionReaderFactory factory = getComponentManager().lookup(TransactionReaderFactory.class, "lcl");
        this.reader = factory.createTransactionReader(getClass().getResourceAsStream("/example.qif"), configuration);
    }

    @Test
    public void testParse() throws Exception
    {
        Transaction transaction = this.reader.next();

        Assert.assertNotNull(transaction);

        Assert.assertEquals(-66.50F, transaction.getValue());
        Assert.assertEquals(new Date(111, 3, 11), transaction.getDate());
        Assert.assertEquals(new Date(111, 3, 9), transaction.getRealDate());
        Assert.assertEquals("Carte", transaction.getType());
        Assert.assertEquals("CB  SNCF", transaction.getDescription());

        transaction = this.reader.next();
        
        Assert.assertNotNull(transaction);

        Assert.assertEquals(-35.31F, transaction.getValue());
        Assert.assertEquals(new Date(111, 3, 12), transaction.getDate());
        Assert.assertNull(transaction.getRealDate());
        Assert.assertEquals("Pr\u00e9l\u00e8vement", transaction.getType());
        Assert.assertEquals("PRLV Free Telecom", transaction.getDescription());

        for (Transaction t = reader.next(); transaction != null; transaction = reader.next()) {
            
        }
        
        this.reader.close();
    }
}
