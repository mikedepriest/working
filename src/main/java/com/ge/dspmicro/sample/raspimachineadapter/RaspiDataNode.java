/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.dspmicro.sample.raspimachineadapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import com.ge.dspmicro.machinegateway.types.PDataNode;

/**
 * 
 * 
 * @author Predix Machine Sample
 */
public class RaspiDataNode extends PDataNode
{
    private RaspiSensor sensor;

    /**
     * Constructor
     * 
     * @param machineAdapterId a unique id
     * @param aSensor RaspiSensor definition for node
     */
    public RaspiDataNode(UUID machineAdapterId, RaspiSensor aSensor)
    {
        super(machineAdapterId, aSensor.getName());

        // Do other initialization if needed.
    	this.sensor = aSensor;
    	this.setDescription(this.sensor.getDescription());
    	this.setEngineeringUnit(this.sensor.getUom());
    }

    /**
     * Node address to uniquely identify the node.
     */
    @Override
    public URI getAddress()
    {
        try
        {
            URI address = new URI("raspi.adapter", null, "localhost", -1, "/" + getName(), null, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return address;
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }
    
    /**
     * @return - this node's sensor
     */
    public RaspiSensor getSensor() {
    	return this.sensor;
    }

}
