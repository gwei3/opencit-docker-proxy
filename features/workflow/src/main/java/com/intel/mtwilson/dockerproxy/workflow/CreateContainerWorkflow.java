package com.intel.mtwilson.dockerproxy.workflow;

import java.io.IOException;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.dockerproxy.common.DockerProxyCache;
import com.intel.mtwilson.dockerproxy.common.ProxyUtil;
import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

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
		// When the user issues a command docker run nginx:latest , an dimage id
		// is not found.
		// we are looking for cases when user issues docker run <short image id>
		if (StringUtils.isNotBlank(imageId)) {
			// Check for short image id
			if (imageId.length() != 64) {
				String command = "docker images --no-trunc | grep " + imageId;
				try {
					Result result = ExecUtil.executeQuoted("/bin/bash", "-c", command);
					if (result.getStderr() != null && StringUtils.isNotEmpty(result.getStderr())) {
						log.error(result.getStderr());
					}
					log.info(result.getStdout());

					String[] split = result.getStdout().split(" ");
					for (String string : split) {
						if (string.indexOf(imageId) != -1) {
							log.info("image id = {}", string);
							if (string.indexOf(":") != 1) {
								String[] split2 = string.split(":");
								String string2 = split2[1];
								log.info("Extracted image id : {}", string2);
								imageId = string2;
							} else {
								imageId = string;
							}
							break;
						}
					}

				} catch (ExecuteException e) {
					String error = "Unable to execute command to find the full image id for imageid=" + imageId;
					log.error(error, e);
					throw new DockerProxyException(error, e);
				} catch (IOException e) {
					String error = "Unable to execute command to find the full image id for imageid=" + imageId;
					log.error(error, e);
					throw new DockerProxyException(error, e);
				}
			}
		}
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
