package com.intel.mtwilson.dockerproxy.workflow;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.common.DockerProxyCache;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.dockerproxy.common.ProxyUtil;

public class CreateContainerWorkflow extends BypassWorkflow {
	private static final Logger log = LoggerFactory.getLogger(CreateContainerWorkflow.class);

	public CreateContainerWorkflow(String requestMethod, String requestUri, String body) {
		super(requestMethod, requestUri, body);
		// TODO Auto-generated constructor stub
	}

	public CreateContainerWorkflow(String requestMethod, String requestUri) {
		super(requestMethod, requestUri);
		// TODO Auto-generated constructor stub
	}

	/**
	 * this class does not need to process the client request. It extracts the
	 * generated container id from the response and adds the mapping of
	 * container to image id in the cache.
	 */
	@Override
	public void processResponseFromEngine() throws DockerProxyException {
		log.debug("Going to extract containerId and imageId for request uri::{}", requestUri);
		String imageId = ProxyUtil.extractImageIdFromCreateContainerRequest(requestBody);
		log.debug("CreateContainerWorkflow, extracted imageId(from request) imageId::" + imageId);
		String containerId = ProxyUtil.extractContainerIdFromCreateContainerResponse(responseBody);
		log.debug("CreateContainerWorkflow, extracted containerId(from response) containerId::" + containerId);
		log.debug("CreateContainerWorkflow processResponseFromEngine imageId:{} and containerId:{} ", imageId,
				containerId);
		if (StringUtils.isNotBlank(containerId) && StringUtils.isNotBlank(imageId)) {
			DockerProxyCache.setImageIdForContainer(containerId, imageId);
		}
	}
}
