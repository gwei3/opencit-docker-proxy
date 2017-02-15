package com.intel.mtwilson.dockerproxy.workflow;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.common.DockerProxyCache;
import com.intel.mtwilson.dockerproxy.common.MountUtil;
import com.intel.mtwilson.dockerproxy.common.ProxyThreadExecutor;
import com.intel.mtwilson.dockerproxy.common.UnmountTask;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.dockerproxy.common.ProxyUtil;



public class StartWorkflow extends BypassWorkflow {

	public StartWorkflow(String requestMethod, String requestUri, String body) {
		super(requestMethod, requestUri, body);
	}

	public StartWorkflow(String requestMethod, String requestUri) {
		super(requestMethod, requestUri);
	}

	private static final Logger log = LoggerFactory.getLogger(StartWorkflow.class);

	@Override
	public boolean validateClientRequestAndInit() throws DockerProxyException {

		// Get the container id from the START request
		String containerId = ProxyUtil.extractContainerIdformStartRequest(requestUri);
		log.debug("Validating start request , containerId:: {}", containerId);
		// Get the associated image id from the cache
		String imageId = DockerProxyCache.getImageIdForContainer(containerId);
		if (StringUtils.isBlank(imageId)) {
			// If imageId is not found we try to get imageId from containerId
			// using container configuration json object
			log.debug("StartWorkflow , containerId:: {} and imageId:: {}" ,containerId ,imageId);
			imageId = ProxyUtil.extractImageIdFromContainerId(containerId);
			// Setting in cache for future use
			if (StringUtils.isNotBlank(containerId)) {
				DockerProxyCache.setImageIdForContainer(containerId, imageId);
			}
		}

		if (StringUtils.isBlank(containerId) || StringUtils.isBlank(imageId)) {
			throw new DockerProxyException("Invalid container id : {} " + (containerId == null ? "NULL" : containerId)
					+ " or image id : " + (imageId == null ? "NULL" : imageId));
		}

		// Call Mount Script
		log.debug("Going to mount image with imageid:: {} and containerId: {}", imageId, containerId);
		MountUtil.mountImage(containerId, imageId);

		// Call PA to validate and check whether to go forward
		log.debug("Going to  invoke policyagent for validation with imageid:: {} and containerId:: {}", imageId,
				containerId);
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		boolean policyAgentValidate;
		try {
			/* Calling PA to validate and check whether to go forward. PA calls vrtm and vrtm calls MA where measurement is carried out and 
			 * cumulative hash is generated. vrtm validates that with policy image hash and returns to PA.
			 */
			policyAgentValidate = ProxyUtil.policyAgentValidate(containerId, imageId);
			log.debug("Policy agent validate status:{} for request uri {}", policyAgentValidate,requestUri);

			if (!policyAgentValidate) { // If PA validation failed we throw
				log.info("\n Policy agent validation failed , requestUri:: {}", requestUri);
			}else{
				log.info("\n Policy agent validation successful, going to launch , requestUri:: {}",requestUri);
			}

			endTime = System.currentTimeMillis();
		} finally {
			if (endTime != 0) {
				long diff = endTime - startTime;
				log.info("StartWorkflow, validateClientRequestAndInit ,PolicyAgent Call time execution :: {}", diff);
			}
			///Asynchronously calling unmount
			UnmountTask unmountTask= new UnmountTask(imageId,containerId);
			ProxyThreadExecutor.submitTask(unmountTask);
		}
		
		return policyAgentValidate;
	}
}
