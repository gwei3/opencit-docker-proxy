package com.intel.mtwilson.dockerproxy.service;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.dockerproxy.api.DockerEngineRequest;
import com.intel.mtwilson.dockerproxy.api.PluginResponse;
import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;

public class ProxyRequestHandler extends GenericHandler {
	private static final PluginResponse EMPTY_ALLOW_RESPONSE = new PluginResponse(true, Constants.EMPTY,
			Constants.EMPTY);

	private static final Logger log = LoggerFactory.getLogger(ProxyRequestHandler.class);

	public String request = null;

	public ProxyRequestHandler(String request) throws DockerProxyException {
		super();
		this.request = request;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			dataFromDocker = objectMapper.readValue(request, DockerEngineRequest.class);
			//dataFromDocker = engineRequest;
		} catch (IOException e) {
			log.error("Error in mapping request to DockerEngineRequest", e);
			throw new DockerProxyException("Error in mapping request to DockerEngineRequest", e);
		}
	}

	@Override
	public PluginResponse processSpecific() throws DockerProxyException {
		/// We validate request to be forwarded for execution to docker engine or deny execution. 
		//Policy agent call is made in this validation
		if(requestWorkflow.validateClientRequestAndInit()){
			return EMPTY_ALLOW_RESPONSE;
		}
		return PluginResponse.CreateDenyPluginResponse("Policy Agent validation Failed");
		///return EMPTY_ALLOW_RESPONSE;
	}

}
