/*
 * Copyright (c) 2016 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
 
package com.ge.dspmicro.sample.raspimachineadapter;

import com.ge.dspmicro.machinegateway.types.PQuality.QualityEnum;

/**
 * 
 * @author predix -
 */
public class RaspiSensorValue {
    private float currentValue;
    private QualityEnum currentQuality = QualityEnum.UNCERTAIN;
    
    /**
     * @param value - new value
     * @param quality - new quality
     */
    public void set(float value, QualityEnum quality) {
    	this.currentValue = value;
    	this.currentQuality = quality;
    }
    
    /**
     * @return -
     */
    public float getValue() {
    	return this.currentValue;
    }
    
    /**
     * @return -
     */
    public QualityEnum getQuality() {
    	return this.currentQuality;
    }
}
