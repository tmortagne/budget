package org.mortagne.budget.transaction.io;

import java.text.DateFormat;

public class TransactionReaderConfiguration
{
    private DateFormat dateFormat;

    private String charset;

    public void setDateFormat(DateFormat dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    public DateFormat getDateFormat()
    {
        return this.dateFormat;
    }

    public String getCharset()
    {
        return this.charset;
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }
}
