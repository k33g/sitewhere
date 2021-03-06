/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.server;

import java.util.List;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sitewhere.server.lifecycle.LifecycleComponentDecorator;
import com.sitewhere.spi.ServerStartupException;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.IAssetManagement;
import com.sitewhere.spi.asset.IAssetModuleManager;
import com.sitewhere.spi.configuration.IConfigurationResolver;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceManagementCacheProvider;
import com.sitewhere.spi.device.communication.IDeviceCommunication;
import com.sitewhere.spi.device.event.processor.IInboundEventProcessorChain;
import com.sitewhere.spi.device.event.processor.IOutboundEventProcessorChain;
import com.sitewhere.spi.scheduling.IScheduleManagement;
import com.sitewhere.spi.scheduling.IScheduleManager;
import com.sitewhere.spi.search.external.ISearchProviderManager;
import com.sitewhere.spi.server.ISiteWhereServer;
import com.sitewhere.spi.server.ISiteWhereServerRuntime;
import com.sitewhere.spi.server.ISiteWhereServerState;
import com.sitewhere.spi.server.ISiteWhereTenantEngine;
import com.sitewhere.spi.server.debug.ITracer;
import com.sitewhere.spi.server.lifecycle.ILifecycleComponent;
import com.sitewhere.spi.system.IVersion;
import com.sitewhere.spi.user.ITenant;
import com.sitewhere.spi.user.IUserManagement;

/**
 * Wraps an {@link ISiteWhereServer} implementation so new functionality can be triggered
 * by API calls withhout changing core server logic.
 * 
 * @author Derek
 */
public class SiteWhereServerDecorator extends LifecycleComponentDecorator implements ISiteWhereServer {

	/** Delegate instance being wrapped */
	private ISiteWhereServer server;

	public SiteWhereServerDecorator(ISiteWhereServer server) {
		super(server);
		this.server = server;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getVersion()
	 */
	@Override
	public IVersion getVersion() {
		return server.getVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getServerState()
	 */
	@Override
	public ISiteWhereServerState getServerState() {
		return server.getServerState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getServerState(boolean)
	 */
	@Override
	public ISiteWhereServerRuntime getServerRuntimeInformation(boolean includeHistorical)
			throws SiteWhereException {
		return server.getServerRuntimeInformation(includeHistorical);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#initialize()
	 */
	@Override
	public void initialize() throws SiteWhereException {
		server.initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getServerStartupError()
	 */
	@Override
	public ServerStartupException getServerStartupError() {
		return server.getServerStartupError();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#setServerStartupError(com.sitewhere.spi
	 * .ServerStartupException)
	 */
	@Override
	public void setServerStartupError(ServerStartupException e) {
		server.setServerStartupError(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getTracer()
	 */
	@Override
	public ITracer getTracer() {
		return server.getTracer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getConfigurationResolver()
	 */
	@Override
	public IConfigurationResolver getConfigurationResolver() {
		return server.getConfigurationResolver();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getTenantByAuthToken(java.lang.String)
	 */
	@Override
	public ITenant getTenantByAuthToken(String authToken) throws SiteWhereException {
		return server.getTenantByAuthToken(authToken);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getAuthorizedTenants(java.lang.String)
	 */
	@Override
	public List<ITenant> getAuthorizedTenants(String userId) throws SiteWhereException {
		return server.getAuthorizedTenants(userId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getTenantEngine(java.lang.String)
	 */
	@Override
	public ISiteWhereTenantEngine getTenantEngine(String tenantId) throws SiteWhereException {
		return server.getTenantEngine(tenantId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#onTenantInformationUpdated(com.sitewhere
	 * .spi.user.ITenant)
	 */
	@Override
	public void onTenantInformationUpdated(ITenant tenant) throws SiteWhereException {
		server.onTenantInformationUpdated(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getUserManagement()
	 */
	@Override
	public IUserManagement getUserManagement() {
		return server.getUserManagement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getDeviceManagement(com.sitewhere.spi
	 * .user.ITenant)
	 */
	@Override
	public IDeviceManagement getDeviceManagement(ITenant tenant) throws SiteWhereException {
		return server.getDeviceManagement(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getDeviceManagementCacheProvider(com.
	 * sitewhere.spi.user.ITenant)
	 */
	@Override
	public IDeviceManagementCacheProvider getDeviceManagementCacheProvider(ITenant tenant)
			throws SiteWhereException {
		return server.getDeviceManagementCacheProvider(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getAssetManagement(com.sitewhere.spi.
	 * user.ITenant)
	 */
	@Override
	public IAssetManagement getAssetManagement(ITenant tenant) throws SiteWhereException {
		return server.getAssetManagement(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getScheduleManagement(com.sitewhere.spi
	 * .user.ITenant)
	 */
	@Override
	public IScheduleManagement getScheduleManagement(ITenant tenant) throws SiteWhereException {
		return server.getScheduleManagement(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getDeviceCommunication(com.sitewhere.
	 * spi.user.ITenant)
	 */
	@Override
	public IDeviceCommunication getDeviceCommunication(ITenant tenant) throws SiteWhereException {
		return server.getDeviceCommunication(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getOutboundEventProcessorChain(com.sitewhere
	 * .spi.user.ITenant)
	 */
	@Override
	public IOutboundEventProcessorChain getOutboundEventProcessorChain(ITenant tenant)
			throws SiteWhereException {
		return server.getOutboundEventProcessorChain(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getInboundEventProcessorChain(com.sitewhere
	 * .spi.user.ITenant)
	 */
	@Override
	public IInboundEventProcessorChain getInboundEventProcessorChain(ITenant tenant)
			throws SiteWhereException {
		return server.getInboundEventProcessorChain(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getAssetModuleManager(com.sitewhere.spi
	 * .user.ITenant)
	 */
	@Override
	public IAssetModuleManager getAssetModuleManager(ITenant tenant) throws SiteWhereException {
		return server.getAssetModuleManager(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getSearchProviderManager(com.sitewhere
	 * .spi.user.ITenant)
	 */
	@Override
	public ISearchProviderManager getSearchProviderManager(ITenant tenant) throws SiteWhereException {
		return server.getSearchProviderManager(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getScheduleManager(com.sitewhere.spi.
	 * user.ITenant)
	 */
	@Override
	public IScheduleManager getScheduleManager(ITenant tenant) throws SiteWhereException {
		return server.getScheduleManager(tenant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getRegisteredLifecycleComponents()
	 */
	@Override
	public List<ILifecycleComponent> getRegisteredLifecycleComponents() {
		return server.getRegisteredLifecycleComponents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sitewhere.spi.server.ISiteWhereServer#getLifecycleComponentById(java.lang.String
	 * )
	 */
	@Override
	public ILifecycleComponent getLifecycleComponentById(String id) {
		return server.getLifecycleComponentById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getMetricRegistry()
	 */
	@Override
	public MetricRegistry getMetricRegistry() {
		return server.getMetricRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sitewhere.spi.server.ISiteWhereServer#getHealthCheckRegistry()
	 */
	@Override
	public HealthCheckRegistry getHealthCheckRegistry() {
		return server.getHealthCheckRegistry();
	}
}