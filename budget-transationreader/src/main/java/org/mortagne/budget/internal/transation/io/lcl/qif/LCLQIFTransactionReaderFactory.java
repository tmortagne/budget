package org.mortagne.budget.internal.transation.io.lcl.qif;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.Locale;

import org.mortagne.budget.transation.io.TransactionReader;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;
import org.mortagne.budget.transation.io.TransactionReaderFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.logging.AbstractLogEnabled;

@Component("lcl.qif")
public class LCLQIFTransactionReaderFactory extends AbstractLogEnabled implements TransactionReaderFactory
{
    public String getName()
    {
        return "LCL QIF";
    }

    public String getDescription()
    {
        return "LCL French QIF format";
    }

    public TransactionReader createTransactionReader(InputStream transationStream,
        TransactionReaderConfiguration configuration)
    {
        configuration.setDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE));
        configuration.setCharset("ISO-8859-1");

        LCLQIFTransactionReader reader = new LCLQIFTransactionReader(transationStream, configuration);
        reader.enableLogging(getLogger());
        
        return reader;
    }
}
