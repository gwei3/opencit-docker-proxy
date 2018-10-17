package com.intel.mtwilson.dockerproxy.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.dockerproxy.api.DockerEngineResponse;
import com.intel.mtwilson.dockerproxy.api.PluginResponse;
import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;

public class ProxyResponseHandler extends GenericHandler {

	private static final Logger log = LoggerFactory.getLogger(ProxyRequestHandler.class);

	public String request = null;
	public DockerEngineResponse engineResponse = null;

	public ProxyResponseHandler(String request) throws DockerProxyException {
		super();
		this.request = request;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			engineResponse = objectMapper.readValue(request, DockerEngineResponse.class);
			dataFromDocker = engineResponse;
		} catch (IOException e) {
			log.error("Error in mapping request to DockerEngineResponse", e);
			throw new DockerProxyException("Error in mapping request to DockerEngineResponse", e);
		}
	}

	@Override
	public PluginResponse processSpecific() throws DockerProxyException {

		byte[] responseBodyBytes = engineResponse.ResponseBody;
		String responseBody = null;
		if (responseBodyBytes != null) {
			responseBody = new String(responseBodyBytes);
		}
		// Set response body and status in workflow
		requestWorkflow.setResponseBody(responseBody);
		requestWorkflow.setResponseStatus(String.valueOf(engineResponse.ResponseStatusCode));

		/// Calling processResponseFromEngine which updates vrtm
		requestWorkflow.processResponseFromEngine();
		return new PluginResponse(true, Constants.EMPTY, Constants.EMPTY);
	}

}
