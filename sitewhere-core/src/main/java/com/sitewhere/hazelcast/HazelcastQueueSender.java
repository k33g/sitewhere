/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.hazelcast;

import org.apache.log4j.Logger;

import com.hazelcast.core.IQueue;
import com.sitewhere.device.event.processor.InboundEventProcessor;
import com.sitewhere.rest.model.device.communication.DecodedDeviceRequest;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceRegistrationRequest;
import com.sitewhere.spi.device.event.request.IDeviceStreamCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceStreamDataCreateRequest;
import com.sitewhere.spi.device.event.request.ISendDeviceStreamDataRequest;
import com.sitewhere.spi.server.hazelcast.ISiteWhereHazelcast;

/**
 * Sends all events to a Hazelcast queue.
 * 
 * @author Derek
 */
public class HazelcastQueueSender extends InboundEventProcessor {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(HazelcastQueueSender.class);

	/** Common Hazelcast configuration */
	private SiteWhereHazelcastConfiguration configuration;

	/** Queue of events to be processed */
	private IQueue<DecodedDeviceRequest<?>> eventQueue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.device.event.processor.InboundEventProcessor#start()
	 */
	@Override
	public void start() throws SiteWhereException {
		if (getConfiguration() == null) {
			throw new SiteWhereException("No Hazelcast configuration provided.");
		}
		this.eventQueue =
				getConfiguration().getHazelcastInstance().getQueue(ISiteWhereHazelcast.QUEUE_ALL_EVENTS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.device.event.processor.InboundEventProcessor#onRegistrationRequest
	 * (java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceRegistrationRequest)
	 */
	@Override
	public void onRegistrationRequest(String hardwareId, String originator, IDeviceRegistrationRequest request)
			throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceRegistrationRequest>(hardwareId, originator, request));
			LOGGER.debug("Sent event for " + hardwareId + " to Hazelcast event queue.");
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.device.event.processor.InboundEventProcessor#
	 * onDeviceCommandResponseRequest(java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest)
	 */
	@Override
	public void onDeviceCommandResponseRequest(String hardwareId, String originator,
			IDeviceCommandResponseCreateRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceCommandResponseCreateRequest>(hardwareId, originator,
							request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.device.event.processor.InboundEventProcessor#
	 * onDeviceMeasurementsCreateRequest(java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceMeasurementsCreateRequest)
	 */
	@Override
	public void onDeviceMeasurementsCreateRequest(String hardwareId, String originator,
			IDeviceMeasurementsCreateRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceMeasurementsCreateRequest>(hardwareId, originator,
							request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.device.event.processor.InboundEventProcessor#
	 * onDeviceLocationCreateRequest(java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest)
	 */
	@Override
	public void onDeviceLocationCreateRequest(String hardwareId, String originator,
			IDeviceLocationCreateRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceLocationCreateRequest>(hardwareId, originator, request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.device.event.processor.InboundEventProcessor#onDeviceAlertCreateRequest
	 * (java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest)
	 */
	@Override
	public void onDeviceAlertCreateRequest(String hardwareId, String originator,
			IDeviceAlertCreateRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceAlertCreateRequest>(hardwareId, originator, request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.device.event.processor.InboundEventProcessor#onDeviceStreamCreateRequest
	 * (java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceStreamCreateRequest)
	 */
	@Override
	public void onDeviceStreamCreateRequest(String hardwareId, String originator,
			IDeviceStreamCreateRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceStreamCreateRequest>(hardwareId, originator, request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.device.event.processor.InboundEventProcessor#
	 * onDeviceStreamDataCreateRequest(java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.IDeviceStreamDataCreateRequest)
	 */
	@Override
	public void onDeviceStreamDataCreateRequest(String hardwareId, String originator,
			IDeviceStreamDataCreateRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<IDeviceStreamDataCreateRequest>(hardwareId, originator, request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.device.event.processor.InboundEventProcessor#
	 * onSendDeviceStreamDataRequest(java.lang.String, java.lang.String,
	 * com.sitewhere.spi.device.event.request.ISendDeviceStreamDataRequest)
	 */
	@Override
	public void onSendDeviceStreamDataRequest(String hardwareId, String originator,
			ISendDeviceStreamDataRequest request) throws SiteWhereException {
		try {
			getEventQueue().put(
					new DecodedDeviceRequest<ISendDeviceStreamDataRequest>(hardwareId, originator, request));
		} catch (InterruptedException e) {
			handleInterrupted(e);
		}
	}

	/**
	 * Handle case where blocking call is interrupted.
	 * 
	 * @param e
	 */
	protected void handleInterrupted(InterruptedException e) {
		LOGGER.warn("Interrupted while putting event on queue.", e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.lifecycle.ILifecycleComponent#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	public SiteWhereHazelcastConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SiteWhereHazelcastConfiguration configuration) {
		this.configuration = configuration;
	}

	public IQueue<DecodedDeviceRequest<?>> getEventQueue() {
		return eventQueue;
	}

	public void setEventQueue(IQueue<DecodedDeviceRequest<?>> eventQueue) {
		this.eventQueue = eventQueue;
	}
}