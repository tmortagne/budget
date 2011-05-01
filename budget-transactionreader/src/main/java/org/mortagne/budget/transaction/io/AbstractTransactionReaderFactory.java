package org.mortagne.budget.transaction.io;

import org.xwiki.component.logging.AbstractLogEnabled;

public abstract class AbstractTransactionReaderFactory extends AbstractLogEnabled implements TransactionReaderFactory
{
    private String id;

    private String name;

    private String description;

    public AbstractTransactionReaderFactory(String id, String name, String description)
    {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return this.description;
    }
}
