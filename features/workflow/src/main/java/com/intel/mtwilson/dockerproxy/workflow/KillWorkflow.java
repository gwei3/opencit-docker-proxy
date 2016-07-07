package com.intel.mtwilson.dockerproxy.workflow;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.common.ProxyUtil;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.dockerproxy.vrtm.client.VrtmManager;

public class KillWorkflow extends BypassWorkflow {
	private static final Logger log = LoggerFactory.getLogger(KillWorkflow.class);

	public KillWorkflow(String requestMethod, String requestUri, String body) {
		super(requestMethod, requestUri, body);
		// TODO Auto-generated constructor stub
	}

	public KillWorkflow(String requestMethod, String requestUri) {
		super(requestMethod, requestUri);
		// TODO Auto-generated constructor stub
	}

	/**
	 * this method extracts the container id from the response which was
	 * 'killed' and then updates the status of it in vrtm
	 * 
	 */
	@Override
	public void processResponseFromEngine() throws DockerProxyException {
		int responseStatusInt = Integer.parseInt(responseStatus);
		if (responseStatusInt == HttpStatus.SC_NO_CONTENT || responseStatusInt == HttpStatus.SC_NOT_MODIFIED
				|| responseStatusInt == HttpStatus.SC_OK) {
			String containerId = ProxyUtil.extractContainerIdformKillRequest(requestUri);
			VrtmManager vrtmManager = new VrtmManager();
			vrtmManager.checkAndUpdateVrtm(containerId, Constants.VRTM_STATUS_STOPPED);
		}
	}

}
