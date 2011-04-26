package org.mortagne.budget.internal.transaction.io.lcl.pdf;

import java.io.InputStream;

import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.mortagne.budget.transaction.io.TransactionReaderFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.logging.AbstractLogEnabled;

@Component("lcl.pdf")
public class LCLPDFTransactionReaderFactory extends AbstractLogEnabled implements TransactionReaderFactory
{
    public String getName()
    {
        return "LCL PDF";
    }

    public String getDescription()
    {
        return "LCL PDF based \"RELEVE DE COMPTE\"";
    }

    public TransactionReader createTransactionReader(InputStream transationStream,
        TransactionReaderConfiguration configuration)
    {
        LCLPDFTransactionReader reader =  new LCLPDFTransactionReader(transationStream, configuration);
        
        reader.enableLogging(getLogger());
        
        return reader;
    }
}
