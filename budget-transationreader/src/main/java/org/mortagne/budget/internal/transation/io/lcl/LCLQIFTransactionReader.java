package org.mortagne.budget.internal.transation.io.lcl;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.mortagne.budget.internal.transation.io.qif.QIFTransactionReader;
import org.mortagne.budget.transation.DefaultTransaction;
import org.mortagne.budget.transation.Transaction;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;

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
