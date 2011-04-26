package org.mortagne.budget.transation.io.qif;

import java.sql.Date;
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

        Assert.assertEquals(500f, transaction.getValue());
        Assert.assertEquals(DATEFORMAT.parse("09.10.09"), transaction.getDate());
        Assert.assertEquals(DATEFORMAT.parse("09.10.09"), transaction.getRealDate());
        // Assert.assertEquals("Carte", transaction.getType());
        Assert.assertEquals("VIREMENT CHAMBON", transaction.getDescription());

        transaction = this.reader.next();

        Assert.assertNotNull(transaction);

        Assert.assertEquals(-35.87f, transaction.getValue());
        Assert.assertEquals(DATEFORMAT.parse("12.10.09"), transaction.getDate());
        Assert.assertEquals(DATEFORMAT.parse("12.10.09"), transaction.getRealDate());
        // Assert.assertEquals("Pr\u00e9l\u00e8vement", transaction.getType());
        Assert.assertEquals("PRLV DEDIBOX SAS", transaction.getDescription());

        for (Transaction t = reader.next(); transaction != null; transaction = reader.next()) {

        }

        this.reader.close();
    }
}
