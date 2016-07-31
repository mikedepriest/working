/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.dspmicro.sample.subscriptionmachineadapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.ge.dspmicro.machinegateway.api.adapter.AbstractSubscriptionMachineAdapter;
import com.ge.dspmicro.machinegateway.api.adapter.IDataSubscription;
import com.ge.dspmicro.machinegateway.api.adapter.IDataSubscriptionListener;
import com.ge.dspmicro.machinegateway.api.adapter.IMachineAdapter;
import com.ge.dspmicro.machinegateway.api.adapter.ISubscriptionMachineAdapter;
import com.ge.dspmicro.machinegateway.api.adapter.MachineAdapterException;
import com.ge.dspmicro.machinegateway.api.adapter.MachineAdapterInfo;
import com.ge.dspmicro.machinegateway.api.adapter.MachineAdapterState;
import com.ge.dspmicro.machinegateway.types.PDataNode;
import com.ge.dspmicro.machinegateway.types.PDataValue;


/**
 * 
 * @author Predix Machine Sample
 */
@SuppressWarnings("javadoc")
@Component(name = RaspiSubscriptionMachineAdapterImpl.SERVICE_PID, provide =
{
        ISubscriptionMachineAdapter.class, IMachineAdapter.class
}, designate = RaspiSubscriptionMachineAdapterImpl.Config.class, configurationPolicy = ConfigurationPolicy.require)
public class RaspiSubscriptionMachineAdapterImpl
        extends AbstractSubscriptionMachineAdapter
{
    // Meta mapping for configuration properties
    @Meta.OCD(name = "%component.name", localization = "OSGI-INF/l10n/bundle")
    interface Config
    {
        @Meta.AD(name = "%updateInterval.name", description = "%updateInterval.description", id = UPDATE_INTERVAL, required = false, deflt = "")
        String updateInterval();

        @Meta.AD(name = "%numberOfNodes.name", description = "%numberOfNodes.description", id = NUMBER_OF_NODES, required = false, deflt = "")
        String numberOfNodes();

        @Meta.AD(name = "%adapterName.name", description = "%adapterName.description", id = ADAPTER_NAME, required = false, deflt = "")
        String adapterName();

        @Meta.AD(name = "%adapterDescription.name", description = "%adapterDescription.description", id = ADAPTER_DESCRIPTION, required = false, deflt = "")
        String adapterDescription();

        @Meta.AD(id = DATA_SUBSCRIPTIONS, name = "%dataSubscriptions.name", description = "%dataSubscriptions.description", required = true, deflt = "")
        String dataSubscriptions();
        
        @Meta.AD(name = "%adapterSensors.name", description = "%adapterSensors.description", id = ADAPTER_SENSORS, required = true, deflt = "")
        String adapterSensors();

    }

    /** Service PID for Sample Machine Adapter */
    public static final String                SERVICE_PID         = "com.ge.dspmicro.sample.subscriptionmachineadapter";         //$NON-NLS-1$
    /** Key for Update Interval */
    public static final String                UPDATE_INTERVAL     = SERVICE_PID + ".UpdateInterval";                             //$NON-NLS-1$
    /** Key for number of nodes */
    public static final String                NUMBER_OF_NODES     = SERVICE_PID + ".NumberOfNodes";                              //$NON-NLS-1$
    /** key for machine adapter name */
    public static final String                ADAPTER_NAME        = SERVICE_PID + ".Name";                                       //$NON-NLS-1$
    /** Key for machine adapter description */
    public static final String                ADAPTER_DESCRIPTION = SERVICE_PID + ".Description";                                //$NON-NLS-1$
    /** data subscriptions */
    public static final String                DATA_SUBSCRIPTIONS  = SERVICE_PID + ".DataSubscriptions";                          //$NON-NLS-1$
    /** Sensors **/
    public static final String                ADAPTER_SENSORS     = SERVICE_PID + ".Sensors";                                    //$NON-NLS-1$
    /** The regular expression used to split property values into String array. */
    public final static String                SPLIT_PATTERN       = "\\s*\\|\\s*";                                               //$NON-NLS-1$

    // Create logger to report errors, warning massages, and info messages (runtime Statistics)
    private static final Logger               _logger             = LoggerFactory
                                                                          .getLogger(RaspiSubscriptionMachineAdapterImpl.class);
    private UUID                              uuid                = UUID.randomUUID();
    private Dictionary<String, Object>        props;
    private MachineAdapterInfo                adapterInfo;
    private MachineAdapterState               adapterState;
    private Map<UUID, RaspiDataNode>         dataNodes           = new HashMap<UUID, RaspiDataNode>();

    private int                               updateInterval;

    private Config                            config;
    private String                            sensorString         = new String();
    
    /**
     * Data cache for holding latest data updates
     */
    protected Map<UUID, PDataValue>           dataValueCache      = new ConcurrentHashMap<UUID, PDataValue>();
    private Map<UUID, RaspiDataSubscription> dataSubscriptions   = new HashMap<UUID, RaspiDataSubscription>();

    private IDataSubscriptionListener         dataUpdateHandler   = new RaspiSubscriptionListener();

    /*
     * ###############################################
     * # OSGi service lifecycle management #
     * ###############################################
     */

    /**
     * OSGi component lifecycle activation method
     * 
     * @param ctx component context
     * @throws IOException on fail to load/set configuration properties
     */
    @Activate
    public void activate(ComponentContext ctx)
            throws IOException
    {
        //if ( _logger.isDebugEnabled() )
        //{
            _logger.info("Starting ***** RasPi ***** " + ctx.getBundleContext().getBundle().getSymbolicName()); //$NON-NLS-1$
        //}

        // Get all properties and create nodes.
        this.props = ctx.getProperties();

        this.config = Configurable.createConfigurable(Config.class, ctx.getProperties());

//        this.sensorString =  this.props.get("Sensors").toString();
//        _logger.info( sensorString.toString() ); 
//      
        //_logger.info( this.config.adapterSensors().toString() );
        
        this.updateInterval = Integer.parseInt(this.config.updateInterval());
        int count = Integer.parseInt(this.config.numberOfNodes());
        createNodes(count);

        List<String> sensors = Arrays.asList(parseSensors());
        
        for (String sensor : sensors)
        {
        	createNode(sensor);
        }
        
        this.adapterInfo = new MachineAdapterInfo(this.config.adapterName(),
                RaspiSubscriptionMachineAdapterImpl.SERVICE_PID, this.config.adapterDescription(), ctx
                        .getBundleContext().getBundle().getVersion().toString());

        List<String> subs = Arrays.asList(parseDataSubscriptions());
        // Start data subscription and sign up for data updates.
        for (String sub : subs)
        {
            RaspiDataSubscription dataSubscription = new RaspiDataSubscription(this, sub, this.updateInterval,
                    new ArrayList<RaspiDataNode>(this.dataNodes.values()));
            this.dataSubscriptions.put(dataSubscription.getId(), dataSubscription);
            // Using internal listener, but these subscriptions can be used with Spillway listener also
            dataSubscription.addDataSubscriptionListener(this.dataUpdateHandler);
            new Thread(dataSubscription).start();
        }
    }
    
    private String[] parseSensors()
    {
        Object objectValue = this.props.get(ADAPTER_SENSORS);

        if ( objectValue == null )
        {
            invalidSensor();
        }

        if ( objectValue instanceof String[] )
        {
            if ( ((String[]) objectValue).length == 0 )
            {
                invalidSensor();
            }
            return (String[]) objectValue;
        }

        @SuppressWarnings("null")
        String stringValue = objectValue.toString();
        if ( stringValue.length() > 0 )
        {
            return stringValue.split(SPLIT_PATTERN);
        }

        invalidSensor();
        return new String[0];
    }

    private void invalidSensor()
    {
        // sensor definitions must not be empty.
        String msg = "RaspiMachineAdapter.Sensors.invalid"; //$NON-NLS-1$
        _logger.error(msg);
        throw new MachineAdapterException(msg);
    }


    private String[] parseDataSubscriptions()
    {
        Object objectValue = this.props.get(DATA_SUBSCRIPTIONS);

        if ( objectValue == null )
        {
            invalidDataSubscription();
        }

        if ( objectValue instanceof String[] )
        {
            if ( ((String[]) objectValue).length == 0 )
            {
                invalidDataSubscription();
            }
            return (String[]) objectValue;
        }

        @SuppressWarnings("null")
        String stringValue = objectValue.toString();
        if ( stringValue.length() > 0 )
        {
            return stringValue.split(SPLIT_PATTERN);
        }

        invalidDataSubscription();
        return new String[0];
    }

    private void invalidDataSubscription()
    {
        // data subscriptions must not be empty.
        String msg = "SampleSubscriptionAdapter.dataSubscriptions.invalid"; //$NON-NLS-1$
        _logger.error(msg);
        throw new MachineAdapterException(msg);
    }

    /**
     * OSGi component lifecycle deactivation method
     * 
     * @param ctx component context
     */
    @Deactivate
    public void deactivate(ComponentContext ctx)
    {
        // Put your clean up code here when container is shutting down
        if ( _logger.isDebugEnabled() )
        {
            _logger.debug("Stopped sample for " + ctx.getBundleContext().getBundle().getSymbolicName()); //$NON-NLS-1$
        }

        Collection<RaspiDataSubscription> values = this.dataSubscriptions.values();
        // Stop random data generation thread.
        for (RaspiDataSubscription sub : values)
        {
            sub.stop();
        }
        this.adapterState = MachineAdapterState.Stopped;
    }

    /**
     * OSGi component lifecycle modified method. Called when
     * the component properties are changed.
     * 
     * @param ctx component context
     */
    @Modified
    public synchronized void modified(ComponentContext ctx)
    {
        // Handle run-time changes to properties.

        this.props = ctx.getProperties();
    }

    /*
     * #######################################
     * # IMachineAdapter interface methods #
     * #######################################
     */

    @Override
    public UUID getId()
    {
        return this.uuid;
    }

    @Override
    public MachineAdapterInfo getInfo()
    {
        return this.adapterInfo;
    }

    @Override
    public MachineAdapterState getState()
    {
        return this.adapterState;
    }

    /*
     * Returns all data nodes. Data nodes are auto-generated at startup.
     */
    @Override
    public List<PDataNode> getNodes()
    {
        return new ArrayList<PDataNode>(this.dataNodes.values());
    }

    /*
     * Reads data from data cache. Data cache always contains latest values.
     */
    @Override
    public PDataValue readData(UUID nodeId)
            throws MachineAdapterException
    {
        if ( this.dataValueCache.containsKey(nodeId) )
        {
            return this.dataValueCache.get(nodeId);
        }

        // Do not return null.
        return new PDataValue(nodeId);
    }

    /*
     * Writes data value into data cache.
     */
    @Override
    public void writeData(UUID nodeId, PDataValue value)
            throws MachineAdapterException
    {
        if ( this.dataValueCache.containsKey(nodeId) )
        {
            // Put data into cache. The value typically should be written to a device node.
            this.dataValueCache.put(nodeId, value);
        }
    }

    /*
     * ###################################################
     * # ISubscriptionMachineAdapter interface methods #
     * ###################################################
     */

    /*
     * Returns list of all subscriptions.
     */
    @Override
    public List<IDataSubscription> getSubscriptions()
    {
        return new ArrayList<IDataSubscription>(this.dataSubscriptions.values());
    }

    /*
     * Adds new data subscription into the list.
     */
    @Override
    public synchronized UUID addDataSubscription(IDataSubscription subscription)
            throws MachineAdapterException
    {
        if ( subscription == null )
        {
            throw new IllegalArgumentException("Subscription is null"); //$NON-NLS-1$
        }

        List<RaspiDataNode> subscriptionNodes = new ArrayList<RaspiDataNode>();

        // Add new data subscription.
        if ( !this.dataSubscriptions.containsKey(subscription.getId()) )
        {
            // Make sure that new subscription contains valid nodes.
            for (PDataNode node : subscription.getSubscriptionNodes())
            {
                if ( !this.dataNodes.containsKey(node.getNodeId()) )
                {
                    throw new MachineAdapterException("Node doesn't exist for this adapter"); //$NON-NLS-1$
                }

                subscriptionNodes.add(this.dataNodes.get(node.getNodeId()));
            }

            // Create new subscription.
            RaspiDataSubscription newSubscription = new RaspiDataSubscription(this, subscription.getName(),
                    subscription.getUpdateInterval(), subscriptionNodes);
            this.dataSubscriptions.put(newSubscription.getId(), newSubscription);
            new Thread(newSubscription).start();
            return newSubscription.getId();
        }

        return null;
    }

    /*
     * Remove data subscription from the list
     */
    @Override
    public synchronized void removeDataSubscription(UUID subscriptionId)
    {
        // Stop subscription, notify all subscribers, and remove subscription
        if ( this.dataSubscriptions.containsKey(subscriptionId) )
        {
            this.dataSubscriptions.get(subscriptionId).stop();
            this.dataSubscriptions.remove(subscriptionId);
        }
    }

    /**
     * get subscription given subscription id.
     */
    @Override
    public IDataSubscription getDataSubscription(UUID subscriptionId)
    {
        if ( this.dataSubscriptions.containsKey(subscriptionId) )
        {
            return this.dataSubscriptions.get(subscriptionId);
        }
        throw new MachineAdapterException("Subscription does not exist"); //$NON-NLS-1$ 
    }

    @SuppressWarnings("deprecation")
    @Override
    public synchronized void addDataSubscriptionListener(UUID dataSubscriptionId, IDataSubscriptionListener listener)
            throws MachineAdapterException
    {
        if ( this.dataSubscriptions.containsKey(dataSubscriptionId) )
        {
            this.dataSubscriptions.get(dataSubscriptionId).addDataSubscriptionListener(listener);
            return;
        }
        throw new MachineAdapterException("Subscription does not exist"); //$NON-NLS-1$	
    }

    @SuppressWarnings("deprecation")
    @Override
    public synchronized void removeDataSubscriptionListener(UUID dataSubscriptionId, IDataSubscriptionListener listener)
    {
        if ( this.dataSubscriptions.containsKey(dataSubscriptionId) )
        {
            this.dataSubscriptions.get(dataSubscriptionId).removeDataSubscriptionListener(listener);
        }
    }

    /*
     * #####################################
     * # Private methods #
     * #####################################
     */

    /**
     * Generates random nodes
     * 
     * @param count of nodes
     */
    private void createNodes(int count)
    {
        for (int index = 1; index <= count; index++)
        {
            String nodeName = "Node" + Integer.toString(index); //$NON-NLS-1$
            RaspiDataNode node = new RaspiDataNode(this.uuid, nodeName);

            // Create a new node and put it in the cache.
            this.dataNodes.put(node.getNodeId(), node);
        }
    }

    private void createNode(String s)
    {
    	_logger.info("Creating sensor "+s); //$NON-NLS-1$
    	String[] attributes = s.split(","); //$NON-NLS-1$
    	RaspiDataNode node = new RaspiDataNode(this.uuid, attributes[1]);
    	node.setDescription(attributes[2]);
    	node.setEngineeringUnit(attributes[3]);
    	node.setSensorId(attributes[0]);
    	// Create a new node and put it in the cache.
        this.dataNodes.put(node.getNodeId(), node);
    }
    
    // Put data into data cache.
    /**
     * @param values list of values
     */
    protected void putData(List<PDataValue> values)
    {
        for (PDataValue value : values)
        {
            this.dataValueCache.put(value.getNodeId(), value);
        }
    }

}
