package org.mortagne.budget.internal.transaction.io.qif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.mortagne.budget.transaction.DefaultTransaction;
import org.mortagne.budget.transaction.Transaction;
import org.mortagne.budget.transaction.io.TransactionReader;
import org.mortagne.budget.transaction.io.TransactionReaderConfiguration;
import org.xwiki.component.logging.AbstractLogEnabled;

public class QIFTransactionReader extends AbstractLogEnabled implements TransactionReader
{
    private InputStream transationStream;

    private BufferedReader buffer;

    private String type;

    private TransactionReaderConfiguration configuration;

    public QIFTransactionReader(InputStream transationStream, TransactionReaderConfiguration configuration)
    {
        if (transationStream == null) {
            throw new RuntimeException("transationStream argument cannot be null");
        }
        
        this.transationStream = transationStream;
        this.configuration = configuration;
    }

    public TransactionReaderConfiguration getConfiguration()
    {
        return this.configuration;
    }

    public String getType()
    {
        return type;
    }

    public void open() throws IOException
    {
        close();

        this.buffer = new BufferedReader(new InputStreamReader(this.transationStream, this.configuration.getCharset()));

        this.type = this.buffer.readLine();
    }

    public void close() throws IOException
    {
        if (this.buffer != null) {
            this.buffer.close();
        }

        this.type = null;
    }

    public Transaction next() throws IOException, ParseException
    {
        if (this.buffer == null) {
            open();
        }

        if (!hasMore()) {
            return null;
        }

        DefaultTransaction transaction = new DefaultTransaction();

        for (String line = this.buffer.readLine(); line != null && line.charAt(0) != '^'; line = this.buffer.readLine()) {
            char type = line.charAt(0);

            String value = line.substring(1, line.length());

            switch (type) {
                case 'D':
                    if (this.configuration.getDateFormat() != null) {
                        transaction.setDate(this.configuration.getDateFormat().parse(value));
                    }
                    // TODO: use a default DateFormat
                    break;
                case 'M':
                case 'P':
                    transaction.setDescription(value);
                    break;
                case 'N':
                    transaction.setType(value);
                    break;
                case 'T':
                    transaction.setValue(Float.valueOf(value));
                    break;
                default:
                    break;
            }
        }

        return transaction;
    }

    public boolean hasMore()
    {
        boolean result = false;

        try {
            result = this.buffer.ready();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
