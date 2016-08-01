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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.dspmicro.machinegateway.types.PQuality.QualityEnum;

/**
 * 
 * @author predix -
 */
public class RaspiSensor {

    private String id;
    private String name;
    private String description;
    private String uom;
    private String datafile;
 // Create logger to report errors, warning massages, and info messages (runtime Statistics)
    private static final Logger _logger = LoggerFactory.getLogger(RaspiSensor.class);
	 
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

	/**
	 * @return the data file
	 */
	public String getDatafile() {
		return this.datafile;
	}

	/**
	 * @param datafile the data file to set
	 */
	public void setDatafile(String datafile) {
		this.datafile = datafile;
	}
	
	/**
	 * @return - current value
	 */
	public RaspiSensorValue read() {
		RaspiSensorValue retVal = new RaspiSensorValue();
		retVal.set(0, QualityEnum.UNCERTAIN);
		
		Path path = Paths.get(this.datafile);
		RaspiSensor._logger.debug("Reading datafile "+this.datafile); //$NON-NLS-1$
		byte[] fileByteArray = new byte[255] ; // Bigger than the actual file
		String sensorValueString = new String();
		try {
		    Path fp = path.toRealPath();
		    
		    fileByteArray = Files.readAllBytes(fp);
		    sensorValueString = new String(fileByteArray, "ISO-8859-1"); //$NON-NLS-1$
		    RaspiSensor._logger.debug("read data: "+sensorValueString); //$NON-NLS-1$
		    float v = this._parseForValue(sensorValueString);
		    QualityEnum q = this._parseForQuality(sensorValueString);
		    retVal.set(v, q);
		} catch (NoSuchFileException x) {
		    System.err.format("%s: no such" + " file or directory%n", path); //$NON-NLS-1$ //$NON-NLS-2$
		    RaspiSensor._logger.error("read data: "+x.toString()); //$NON-NLS-1$
		    
		} catch (IOException x) {
		    System.err.format("%s%n", x); //$NON-NLS-1$
		    RaspiSensor._logger.error("read data: "+x.toString()); //$NON-NLS-1$
		}
		return retVal;		
	}
	
	/**
	 * @param sensorValueString  
	 */
	private float _parseForValue(String sensorValueString) {
		float retVal = 0;
		/*
		 * String looks like this:
		 * c6 01 4b 46 7f ff 0c 10 bd : crc=bd YES\nc6 01 4b 46 7f ff 0c 10 bd t=28375
		 * For value we want the portion after the second "=", scaled by 1/1000
		 */
		String[] tokens = sensorValueString.split("="); //$NON-NLS-1$
		try {
		    retVal = (Float.parseFloat(tokens[2])) / 1000.0f;
		} catch (NumberFormatException nx) {
			System.err.format("%s%n", nx); //$NON-NLS-1$
		    RaspiSensor._logger.error("read data: "+nx.toString()); //$NON-NLS-1$
		}
		return retVal;
	}
	
	/**
	 * @param sensorValueString  
	 */
	private QualityEnum _parseForQuality(String sensorValueString) {
		QualityEnum retVal = QualityEnum.UNCERTAIN;
		/*
		 * String looks like this:
		 * c6 01 4b 46 7f ff 0c 10 bd : crc=bd YES\nc6 01 4b 46 7f ff 0c 10 bd t=28375
		 * For value we want to replace the newline with space and take
		 * the 12th token separated by " ", converted to a QualityEnum as follows:
		 *  YES = QualityEnum.GOOD
		 *  NO = QualityEnum.BAD
		 *  Anything else, or no value = QualityEnum.UNCERTAIN
		 */
		String svs = sensorValueString.replaceAll("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
		String[] tokens = svs.split(" "); //$NON-NLS-1$
		if (tokens.length > 11) {			
			if (tokens[11].equalsIgnoreCase("YES")) { //$NON-NLS-1$
				retVal = QualityEnum.GOOD;
			} else if (tokens[11].equalsIgnoreCase("NO")) { //$NON-NLS-1$
				retVal = QualityEnum.BAD;
			}
		}
		return retVal;
	}
}
