/*
 * Copyright (c) 2011, Thomas Mortagne. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mortagne.budget.transaction;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class AbstractTransaction implements Transaction
{
    protected String description;

    protected String details;

    protected Date date;

    protected Date realDate;

    protected double value;

    protected double total;

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

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(String details)
    {
        this.details = details;
    }

    public Date getDate()
    {
        return this.date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public Date getRealDate()
    {
        return this.realDate;
    }

    public void setRealDate(Date realDate)
    {
        this.realDate = realDate;
    }

    public double getValue()
    {
        return this.value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }

    public double getTotal()
    {
        return this.total;
    }

    public void setTotal(double total)
    {
        this.total = total;
    }
    
    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
