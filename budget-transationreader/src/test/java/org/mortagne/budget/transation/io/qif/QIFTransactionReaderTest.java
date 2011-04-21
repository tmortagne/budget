package org.mortagne.budget.transation.io.qif;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mortagne.budget.transation.Transaction;
import org.mortagne.budget.transation.io.TransactionReader;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;
import org.mortagne.budget.transation.io.TransactionReaderFactory;
import org.xwiki.test.AbstractComponentTestCase;

public class QIFTransactionReaderTest extends AbstractComponentTestCase
{
    private TransactionReader reader;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        TransactionReaderConfiguration configuration = new TransactionReaderConfiguration();

        configuration.setDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE));
        configuration.setCharset("ISO-8859-1");

        TransactionReaderFactory factory = getComponentManager().lookup(TransactionReaderFactory.class, "qif");
        this.reader = factory.createTransactionReader(getClass().getResourceAsStream("/example.qif"), configuration);
    }

    @Test
    public void testParse() throws Exception
    {
        Transaction transaction = reader.next();

        Assert.assertNotNull(transaction);

        Assert.assertEquals(-66.50F, transaction.getValue());
        Assert.assertEquals(new Date(111, 03, 11), transaction.getDate());
        Assert.assertNull(transaction.getRealDate());
        Assert.assertEquals("Carte", transaction.getType());
        Assert.assertEquals(" CB  SNCF             09/04/11  ", transaction.getDescription());
        
        transaction = reader.next();

        Assert.assertNotNull(transaction);

        Assert.assertEquals(-35.31F, transaction.getValue());
        Assert.assertEquals(new Date(111, 3, 12), transaction.getDate());
        Assert.assertNull(transaction.getRealDate());
        Assert.assertEquals("Pr\u00e9l\u00e8vement", transaction.getType());
        Assert.assertEquals("PRLV Free Telecom               ", transaction.getDescription());
    }
}
