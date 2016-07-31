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

/**
 * 
 * @author predix -
 */
public class RaspiSensors {

    private String id;
    private String name;
    private String description;
    private String uom;
	 
    /**
     * @return -
     */
    public String getId() {
        return this.id;
    }
	 
    /**
     * @return -
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @return -
     */
    public String getDescription() {
        return this.description;
    }
	 
    /**
     * @return -
     */
    public String getUom() {
        return this.uom;
    }
	 
    /**
     * @param sensorId -
     */
    public void setId(String sensorId) {
        this.id = sensorId;
    }
	 
    /**
     * @param sensorName -
     */
    public void setName(String sensorName) {
        this.name = sensorName;
    }
	 
    /**
     * @param sensorDescription -
     */
    public void setDescription(String sensorDescription) {
        this.description = sensorDescription;
    }
	 
    /**
     * @param sensorUom -
     */
    public void setUom(String sensorUom) {
        this.uom = sensorUom;
    }
	 
}
