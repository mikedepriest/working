/*
 * Copyright (c) 2016 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.dspmicro.sample.subscriptionmachineadapter;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.dspmicro.machinegateway.api.adapter.IDataSubscriptionListener;
import com.ge.dspmicro.machinegateway.api.adapter.ISubscriptionMachineAdapter;
import com.ge.dspmicro.machinegateway.types.PDataValue;

/**
 * 
 * @author Predix Machine Sample
 */
public class RaspiSubscriptionListener
        implements IDataSubscriptionListener
{
    private static final Logger _logger = LoggerFactory.getLogger(RaspiSubscriptionListener.class);
    private UUID                id      = UUID.randomUUID();

    /**
     * default constructor
     */
    public RaspiSubscriptionListener()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public UUID getId()
    {
        return this.id;
    }

    @Override
    public void onDataUpdate(ISubscriptionMachineAdapter sender, List<PDataValue> values)
    {
        for (PDataValue value : values)
        {
            _logger.info("Internal Listernal with Value:  " + value); //$NON-NLS-1$
        }
    }

    @Override
    public void onDataError(ISubscriptionMachineAdapter sender)
    {
        // Auto-generated method stub
    }

    @Override
    public void onSubscriptionDelete(ISubscriptionMachineAdapter sender, UUID subscriptionId)
    {
        // Auto-generated method stub
    }
}