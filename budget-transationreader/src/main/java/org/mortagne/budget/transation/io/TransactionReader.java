package org.mortagne.budget.transation.io;

import java.io.IOException;
import java.text.ParseException;

import org.mortagne.budget.transation.Transaction;

public interface TransactionReader
{
    TransactionReaderConfiguration getConfiguration();

    void open() throws IOException;

    void close() throws IOException;

    Transaction next() throws IOException, ParseException;

    boolean hasMore();
}
