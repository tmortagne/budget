package org.mortagne.budget.transation;

import java.util.Date;

public interface Transaction
{
    String getId();

    String getDescription();

    Date getDate();

    Date getRealDate();

    float getValue();

    String getType();
}
