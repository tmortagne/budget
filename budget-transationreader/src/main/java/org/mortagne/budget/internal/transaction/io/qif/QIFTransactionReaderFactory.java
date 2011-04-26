package org.mortagne.budget.internal.transaction.io.qif;

import java.io.InputStream;

import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.mortagne.budget.transaction.io.TransactionReaderFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.logging.AbstractLogEnabled;

@Component("qif")
public class QIFTransactionReaderFactory extends AbstractLogEnabled implements TransactionReaderFactory
{
    public String getName()
    {
        return "Generic QIF";
    }

    public String getDescription()
    {
        return "Generic QIF";
    }

    public TransactionReader createTransactionReader(InputStream transationStream,
        TransactionReaderConfiguration configuration)
    {
        QIFTransactionReader reader =  new QIFTransactionReader(transationStream, configuration);
        
        reader.enableLogging(getLogger());
        
        return reader;
    }
}
