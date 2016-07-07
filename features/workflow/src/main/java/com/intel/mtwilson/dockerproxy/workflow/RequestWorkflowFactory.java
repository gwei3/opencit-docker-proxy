package com.intel.mtwilson.dockerproxy.workflow;

import com.intel.mtwilson.dockerproxy.common.RequestUriFilter;

public class RequestWorkflowFactory {
	public static RequestWorkflow getRequestProcessor(String requestMethod, String requestUri) {
		RequestWorkflow workflowProcessor = null;
		if (RequestUriFilter.isStartRequest(requestMethod, requestUri)) {
			workflowProcessor = new StartWorkflow(requestMethod, requestUri);
		} else if (RequestUriFilter.isCreateContainerRequest(requestMethod, requestUri)) {
			workflowProcessor = new CreateContainerWorkflow(requestMethod, requestUri);
		} else if (RequestUriFilter.isStopRequest(requestMethod, requestUri)) {
			workflowProcessor = new StopWorkflow(requestMethod, requestUri);
		} else if (RequestUriFilter.isKillRequest(requestMethod, requestUri)) {
			workflowProcessor = new KillWorkflow(requestMethod, requestUri);
		} else if (RequestUriFilter.isRestartRequest(requestMethod, requestUri)) {
			workflowProcessor = new RestartWorkflow(requestMethod, requestUri);
		} else if (RequestUriFilter.isDeleteRequest(requestMethod, requestUri)) {
			workflowProcessor = new RMIWorkflow(requestMethod, requestUri);
		} else {
			workflowProcessor = new BypassWorkflow(requestMethod, requestUri);
		}

		return workflowProcessor;
	}
}
