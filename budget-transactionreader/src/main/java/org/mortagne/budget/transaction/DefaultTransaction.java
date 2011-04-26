package org.mortagne.budget.transaction;

import java.util.Date;

public class DefaultTransaction extends AbstractTransaction
{
    public void setDate(Date date)
    {
        this.date = date;
    }

    public void setRealDate(Date realDate)
    {
        this.realDate = realDate;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setDetails(String details)
    {
        this.details = details;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setValue(float value)
    {
        this.value = value;
    }
}
