package com.intel.mtwilson.dockerproxy.service;

import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.api.DockerEngineBaseRequestResponse;
import com.intel.mtwilson.dockerproxy.api.PluginResponse;
import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.dockerproxy.workflow.RequestWorkflow;
import com.intel.mtwilson.dockerproxy.workflow.RequestWorkflowFactory;

public abstract class GenericHandler {
	private static final Logger log = LoggerFactory.getLogger(ProxyRequestHandler.class);

	private static final PluginResponse EMPTY_ALLOW_RESPONSE = new PluginResponse(true, Constants.EMPTY,
			Constants.EMPTY);

	protected DockerEngineBaseRequestResponse dataFromDocker;

	protected RequestWorkflow requestWorkflow;

	public PluginResponse processRequest() throws DockerProxyException {

		String requestUri = dataFromDocker.RequestUri;
		String requestMethod = dataFromDocker.RequestMethod;
		if (requestMethod.equalsIgnoreCase(HttpMethod.GET)) {
			/// We do not process Get request
			log.debug("Not processing GET request : {}", requestUri);
			return EMPTY_ALLOW_RESPONSE;
		}
		
		byte[] requestBodyBytes = dataFromDocker.RequestBody;
		String requestBody = null;
		if (requestBodyBytes != null) {
			requestBody = new String(requestBodyBytes);
		}
		log.debug("body after Base64 decoding:: {}", requestBody);
		log.debug("requestMethod:: {}", requestMethod);
		log.debug("requestUri:: {}", requestUri);
		log.debug("body:: {}", requestBody);
		// Initializing workflow based on request uri and method
		requestWorkflow = RequestWorkflowFactory.getRequestProcessor(requestMethod, requestUri);
		requestWorkflow.setRequestbody(requestBody);
		return processSpecific();
	}

	public abstract PluginResponse processSpecific() throws DockerProxyException;

}
