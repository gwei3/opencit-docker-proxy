package com.intel.mtwilson.dockerproxy.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.common.ProxyUtil;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.dockerproxy.vrtm.client.VrtmManager;

public class RMIWorkflow extends BypassWorkflow {
	private static final Logger log = LoggerFactory.getLogger(RMIWorkflow.class);

	public RMIWorkflow(String requestMethod, String requestUri, String body) {
		super(requestMethod, requestUri, body);
		// TODO Auto-generated constructor stub
	}

	public RMIWorkflow(String requestMethod, String requestUri) {
		super(requestMethod, requestUri);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * This class only implements the method that extracts the container id from
	 * response of the engine and then updates vrtm
	 * 
	 */
	@Override
	public void processResponseFromEngine() throws DockerProxyException {
		int responseStatusInt = Integer.parseInt(responseStatus);
		if (responseStatusInt == HttpStatus.SC_NO_CONTENT || responseStatusInt == HttpStatus.SC_OK) {
			String containerId = ProxyUtil.extractContainerIdformDeleteRequest(requestUri);
			log.debug("RMI workflow containerId:: {}",
					containerId);
			if(StringUtils.isBlank(containerId)){
				log.error("Could not extract container id from request uri:{}. Not updating vRTM status", requestUri);
				return;
			}
			VrtmManager vrtmManager = new VrtmManager();
			vrtmManager.checkAndUpdateVrtm(containerId, Constants.VRTM_STATUS_DELETED);
		}

	}

}
