package com.intel.mtwilson.dockerproxy.workflow;

import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;

/**
 * It is a base abstract class for different workflows. Workflow suclasses need
 * to provide there own implemetation of abstract methods.
 * 
 * @author Siddarth
 *
 */
public abstract class RequestWorkflow {
	public String requestMethod;
	public String requestUri;
	public String requestBody;
	public String responseStatus;
	public String responseBody;

	public RequestWorkflow(String requestMethod, String requestUri, String body) {
		super();
		this.requestMethod = requestMethod;
		this.requestUri = requestUri;
		this.requestBody = body;
	}

	public RequestWorkflow(String requestMethod, String requestUri) {
		super();
		this.requestMethod = requestMethod;
		this.requestUri = requestUri;
	}

	public String getBody() {
		return requestBody;
	}

	public void setBody(String body) {
		this.requestBody = body;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getRequestbody() {
		return requestBody;
	}

	public void setRequestbody(String requestbody) {
		this.requestBody = requestbody;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	/**
	 * This method is kept abstract so that the sub classes can extract required
	 * details from the client request as needed
	 * 
	 * @throws DockerProxyException
	 */
	public abstract boolean validateClientRequestAndInit() throws DockerProxyException;

	/**
	 * This method is kept abstract so that the sub classes can extract the
	 * required details from the engine response as needed.
	 * 
	 * @param response
	 * @throws DockerProxyException
	 */
	public abstract void processResponseFromEngine() throws DockerProxyException;

}
