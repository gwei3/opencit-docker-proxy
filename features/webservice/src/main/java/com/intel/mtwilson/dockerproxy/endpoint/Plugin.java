package com.intel.mtwilson.dockerproxy.endpoint;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.api.PluginActivationResponse;
import com.intel.mtwilson.dockerproxy.api.PluginResponse;
import com.intel.mtwilson.dockerproxy.common.Constants;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.dockerproxy.service.ProxyRequestHandler;
import com.intel.mtwilson.dockerproxy.service.ProxyResponseHandler;
import com.intel.mtwilson.launcher.ws.ext.V2;

@V2
@Path("/")
public class Plugin {
	Logger log = LoggerFactory.getLogger(Plugin.class);

	
	@POST
	@Produces({ MediaType.APPLICATION_JSON, Constants.MEDIATYPE_DOCKER_PLUGIN_v1,
			Constants.MEDIATYPE_DOCKER_PLUGIN_v1_1, Constants.MEDIATYPE_DOCKER_PLUGIN_v1_2 })
	@Path("Plugin.Activate")
	public Response activatePluginRequest(@Context HttpServletRequest httpServletRequest) throws IOException {
		log.info("Inside activatePluginRequest");

		PluginActivationResponse res = new PluginActivationResponse();
		res.pares_Implements = new ArrayList<String>();
		res.pares_Implements.add("authz");

		return Response.ok(res).build();

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, Constants.MEDIATYPE_DOCKER_PLUGIN_v1,
			Constants.MEDIATYPE_DOCKER_PLUGIN_v1_1, Constants.MEDIATYPE_DOCKER_PLUGIN_v1_2, MediaType.TEXT_PLAIN })
	@Produces({ MediaType.APPLICATION_JSON, Constants.MEDIATYPE_DOCKER_PLUGIN_v1,
			Constants.MEDIATYPE_DOCKER_PLUGIN_v1_1, Constants.MEDIATYPE_DOCKER_PLUGIN_v1_2 })
	@Path("AuthZPlugin.AuthZReq")
	public Response processRequest(String req, @Context HttpServletRequest httpServletRequest) throws IOException {
		log.debug("Inside processRequest");
		ProxyRequestHandler requestHandler = null;
		try {
			requestHandler = new ProxyRequestHandler(req);
		} catch (DockerProxyException e) {
			log.error("Unable to initialize request handler", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(PluginResponse.createDenyPluginResponse("Unable to initialize plugin request handler")).build();
		}
		PluginResponse response = null;

		try {
			response = requestHandler.processRequest();
		} catch (DockerProxyException e) {
			log.error("Error processing docker request", e);
			return Response.ok(PluginResponse.createDenyPluginResponse(e.getMessage())).build();
		}
		
		return Response.ok(response).build();

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, Constants.MEDIATYPE_DOCKER_PLUGIN_v1,
			Constants.MEDIATYPE_DOCKER_PLUGIN_v1_1, Constants.MEDIATYPE_DOCKER_PLUGIN_v1_2, MediaType.TEXT_PLAIN })
	@Produces({ MediaType.APPLICATION_JSON, Constants.MEDIATYPE_DOCKER_PLUGIN_v1,
			Constants.MEDIATYPE_DOCKER_PLUGIN_v1_1, Constants.MEDIATYPE_DOCKER_PLUGIN_v1_2 })
	@Path("AuthZPlugin.AuthZRes")
	public Response processResponse(String req, @Context HttpServletRequest httpServletRequest) throws IOException {
		log.debug("Inside processResponse");
		ProxyResponseHandler responseHandler = null;

		try {
			responseHandler = new ProxyResponseHandler(req);
		} catch (DockerProxyException e) {
			log.error("Unable to initialize response handler", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(PluginResponse.createDenyPluginResponse("Unable to initialize plugin response handler")).build();
		}
		PluginResponse response = null;

		try {
			response = responseHandler.processRequest();
		} catch (DockerProxyException e) {
			log.error("Error processing docker response", e);
			return Response.ok(PluginResponse.createDenyPluginResponse(e.getMessage())).build();
		}

		return Response.ok(response).build();

	}
}
