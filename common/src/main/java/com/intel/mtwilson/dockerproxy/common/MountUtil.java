package com.intel.mtwilson.dockerproxy.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;

public class MountUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MountUtil.class);


	public static int mountDocker(String mountpath, String imageId) throws DockerProxyException {
		int exitcode = 1;
		String serverType = DockerProxyCache.getEngineServerType();
		String hostUrlParameter = getDockerOpts(serverType);
		String imageIdParameter = "--image-id=" + imageId;
		String mountPathParameter = "--mount-path=" + mountpath;
		String command = Constants.mountDockerScript + Constants.SPACE + mountPathParameter + Constants.SPACE + hostUrlParameter + Constants.SPACE
				+ imageIdParameter;
		log.debug("\n" + "Mounting the docker image : " + mountpath + "with command: " + command);
		try {
			exitcode = ProxyUtil.executeCommandInExecUtil(Constants.mountDockerScript, mountPathParameter,
					hostUrlParameter, imageIdParameter);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in mounting docker image" + e);
			throw new DockerProxyException("Error in mounting docker image", e);
		}
		return exitcode;
	}

	public static int unmountDocker(String mountpath, String imageId) {
		int exitcode = 1;

		String unmountPathParameter = "--unmount-path=" + mountpath;
		String serverType = DockerProxyCache.getEngineServerType();
		String hostUrlParameter = getDockerOpts(serverType);

		String command = Constants.mountDockerScript + Constants.SPACE + unmountPathParameter;
		log.debug(":\n" + "Unmounting the docker image : " + mountpath + "with command: " + command);
		try {
			exitcode = ProxyUtil.executeCommandInExecUtil(Constants.mountDockerScript, unmountPathParameter,
					hostUrlParameter);
		} catch (IOException e) {
			exitcode = 1;
			log.error("Error in unmounting docker image" + e);

		}
		File mountDirectory = new File(mountpath);
		if (mountDirectory.exists()) {
			mountDirectory.delete();
		}
		return exitcode;
	}

	public static void mountImage(String containerId, String imageId) throws DockerProxyException {
		if (StringUtils.isBlank(imageId)) {
			String msg = "No image id provided for mount";
			log.error(msg);
			throw new DockerProxyException(msg);
		}
		if (StringUtils.isBlank(containerId)) {
			String msg = "No containerId id provided for mount";
			log.error(msg);
			throw new DockerProxyException(msg);
		}
		mountDocker(Constants.MOUNT_PATH + containerId, imageId);
	}

	public static void unmountImage(String containerId, String imageId) {
		if (StringUtils.isBlank(imageId)) {
			log.error("No image id provided for unmount");
			return;
		}
		if (StringUtils.isBlank(containerId)) {
			log.error("No containerId id provided for mount");

			return;
		}
		unmountDocker(Constants.MOUNT_PATH + containerId, imageId);
	}

	private static String getDockerOpts(String serverType) {
		String hostUrlParameter = null;
		if (Constants.SERVER_TYPE_TCP.equalsIgnoreCase(serverType)) {
			hostUrlParameter = "--host=tcp://" + DockerProxyCache.getEngineHost() + ":"
					+ DockerProxyCache.getEnginePort();
		} else {
			hostUrlParameter = "--host=unix://" + DockerProxyCache.getEngineSocket();
		}
		return hostUrlParameter;
	}

}
