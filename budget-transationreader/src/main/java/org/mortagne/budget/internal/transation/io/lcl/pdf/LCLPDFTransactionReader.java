package org.mortagne.budget.internal.transation.io.lcl.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Iterator;

import org.mortagne.budget.transation.DefaultTransaction;
import org.mortagne.budget.transation.Transaction;
import org.mortagne.budget.transation.io.TransactionReader;
import org.mortagne.budget.transation.io.TransactionReaderConfiguration;
import org.xwiki.component.logging.AbstractLogEnabled;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

public class LCLPDFTransactionReader extends AbstractLogEnabled implements TransactionReader
{
    private InputStream transationStream;

    private TransactionReaderConfiguration configuration;

    private Iterator<DefaultTransaction> it = null;

    public LCLPDFTransactionReader(InputStream transationStream, TransactionReaderConfiguration configuration)
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

    public void open() throws IOException
    {
        close();

        LCLLocationTextExtractionStrategy textExtractionStrategy = new LCLLocationTextExtractionStrategy();
        PdfReader reader = new PdfReader(this.transationStream);

        try {
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            parser.processContent(3, textExtractionStrategy);
        } finally {
            reader.close();
        }

        this.it = textExtractionStrategy.getTransactions().iterator();
    }

    public void close() throws IOException
    {
        this.it = null;
    }

    public Transaction next() throws IOException, ParseException
    {
        if (this.it == null) {
            open();
        }

        if (!hasMore()) {
            return null;
        }

        return this.it.next();
    }

    public boolean hasMore()
    {
        return this.it != null && this.it.hasNext();
    }
}
