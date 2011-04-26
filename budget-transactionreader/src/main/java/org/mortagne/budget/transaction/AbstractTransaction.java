package org.mortagne.budget.transaction;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class AbstractTransaction implements Transaction
{
    protected String description;
    
    protected String details;

    protected Date date;

    protected Date realDate;

    protected float value;

    protected String type;

    public String getId()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.date.getTime());
        if (this.realDate != null) {
            buffer.append(this.realDate.getTime());
        }
        buffer.append(this.value);
        if (this.type != null) {
            buffer.append(this.type);
        }
        if (this.description != null) {
            buffer.append(StringUtils.deleteWhitespace(this.description).toUpperCase());
        }

        return buffer.toString();
    }

    public String getDescription()
    {
        return this.description;
    }
    
    public String getDetails()
    {
        return details;
    }

    public Date getDate()
    {
        return this.date;
    }

    public Date getRealDate()
    {
        return this.realDate;
    }

    public float getValue()
    {
        return this.value;
    }

    public String getType()
    {
        return this.type;
    }
}
