package org.mortagne.budget.transaction;

import java.util.Date;

public interface Transaction
{
    String getId();

    String getDescription();
    
    String getDetails();

    Date getDate();

    Date getRealDate();

    float getValue();

    String getType();
}
