package org.mortagne.budget.transaction.io;

import java.io.InputStream;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface TransactionReaderFactory
{
    String getName();

    String getDescription();

    TransactionReader createTransactionReader(InputStream transationStream, TransactionReaderConfiguration configuration);
}
