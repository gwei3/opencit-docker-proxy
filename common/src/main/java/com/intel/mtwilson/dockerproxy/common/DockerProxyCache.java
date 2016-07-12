package com.intel.mtwilson.dockerproxy.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.configuration.ConfigurationFactory;

public class DockerProxyCache {

	public static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerProxyCache.class);
	public static Map<String, String> containerIdToImageIdMap = new HashMap<String, String>();
	public static Map<String, String> containerIdToVrtmStateMap = new HashMap<String, String>();

	public static String dockerInstallationPath;
	public static String policyAgentPath;
	public static int vrtmPort;
	private static String engineHost;
	private static int enginePort;

	private static String engineServerType;
	private static String engineSocket;

	static {
		init();

	}
	static Configuration proxyConfiguration = null;
	public static void init() {
		
		try {
			proxyConfiguration = ConfigurationFactory.getConfiguration();
		} catch (IOException e1) {
			log.error("Unable to read configuration for proxy", e1);
		}

		dockerInstallationPath = proxyConfiguration.get(Constants.DOCKER_INSTALLATION_PATH,
				Constants.DEFAULT_DOCKER_INSTALL_PATH); // if no installation
		policyAgentPath=proxyConfiguration.get(Constants.POLICY_AGENT_PATH,
				Constants.DEFAULT_POLICY_AGENT_PATH);
		
		// path provided ,it
		// takes default
		// installation path as
		// i.e /var/lib/docker
		String vrtmPortString = proxyConfiguration.get(Constants.VRTM_PORT, Constants.DEFAULT_VRTM_PORT);
		try {
			vrtmPort = Integer.parseInt(vrtmPortString);
		} catch (NumberFormatException e) {
			log.error("Invalid vrtm port : {}", vrtmPort, e);

		}

		setDockerEngineParams();
	}

	
	
	public static String getPolicyAgentPath() {
		return policyAgentPath;
	}



	public static void setPolicyAgentPath(String policyAgentPath) {
		DockerProxyCache.policyAgentPath = policyAgentPath;
	}



	public static Configuration getProxyConfiguration() {
		return proxyConfiguration;
	}



	public static void setProxyConfiguration(Configuration proxyConfiguration) {
		DockerProxyCache.proxyConfiguration = proxyConfiguration;
	}



	private static void setDockerEngineParams() {
		String dockerConfigurationFilePath = proxyConfiguration.get(Constants.DOCKER_CONF_FILE_PATH,
				Constants.DEFAULT_DOCKER_CONFIG_PATH); // default path i.e /etc/default/docker

		File file = new File(dockerConfigurationFilePath);

		if (!file.exists()) {
			log.error("Default docker configuration file does not exist");
		}

		FileInputStream fileInput = null;
		Properties properties = new Properties();
		try {
			fileInput = new FileInputStream(file);
			properties.load(fileInput);
		} catch (IOException e1) {
			log.error("Error reading docker config file at location {}", dockerConfigurationFilePath, e1);
		} finally {
			try {
				fileInput.close();
			} catch (IOException e) {
				log.error("Error closing stream", e);
			}
		}
		String dockerOptsString = properties.getProperty("DOCKER_OPTS");
		if (StringUtils.isBlank(dockerOptsString)) {
			dockerOptsString = Constants.DEFAULT_DOCKER_OPT_STRING;
		}

		String[] params = dockerOptsString.split(Constants.SPACE);

		String dockerHostString = null;

		for (String param : params) {
			param = param.trim();
			log.info("param:" + param);

			if (param.contains(Constants.SERVER_TYPE_UNIX) || param.contains(Constants.SERVER_TYPE_TCP)) {
				dockerHostString = param;
			}

		}
		log.info("dockerHostString:" + dockerHostString);

		if (StringUtils.isBlank(dockerHostString)) {
			engineServerType = Constants.SERVER_TYPE_UNIX;// default run
			// on unix
			// socket at
			// /var/run/docker.sock
			engineSocket = Constants.DEFAULT_DOCKER_SOCKET;

		} else if (dockerHostString.indexOf(Constants.SERVER_TYPE_TCP) != -1) {
			String dockerHosrParameterString = dockerHostString
					.substring(dockerHostString.indexOf(Constants.SERVER_TYPE_TCP));
			engineServerType = Constants.SERVER_TYPE_TCP;
			// /config.setEngineServerType(Constants.SERVER_TYPE_TCP);
			// / config.setProxyHostParameter(dockerHostString);
			String[] arr = dockerHosrParameterString.split("//");
			if (arr != null && arr.length > 1) {
				String[] hostPortarr = arr[1].split(":");
				engineHost = hostPortarr[0];
				String portString = hostPortarr[1].replace("\"", "");
				enginePort = Integer.parseInt(portString);
			} else {
				log.error("Invalid DOCKER_OPTS configured", dockerConfigurationFilePath);

			}
		} else {
			// Default to unix socket
			engineServerType = Constants.SERVER_TYPE_UNIX;

			if (dockerHostString.indexOf(Constants.SERVER_TYPE_UNIX) != -1) {
				String dockerHosrParameterString = dockerHostString
						.substring(dockerHostString.indexOf(Constants.SERVER_TYPE_UNIX));
				String[] arr = dockerHosrParameterString.split("//");
				// /log.info("arr::"+arr[0]+" arr1::"+arr[1]);
				if (arr != null && arr.length > 1) {
					// / log.info("arr1::"+arr[1]);
					String socketString = arr[1].replace("\"", "");
					engineSocket = socketString;

				} else {
					log.error("Invalid DOCKER_OPTS configured", dockerConfigurationFilePath);
				}
			}
		}
		
	}

	public static int getVrtmPort() {
		return vrtmPort;
	}

	public static void setVrtmPort(int vrtmPort) {
		DockerProxyCache.vrtmPort = vrtmPort;
	}

	public static String getDockerInstallationPath() {
		return dockerInstallationPath;
	}

	public static void setDockerInstallationPath(String dockerInstallationPath) {
		DockerProxyCache.dockerInstallationPath = dockerInstallationPath;
	}

	public static Map<String, String> getContainerIdToVrtmStateMap() {
		return containerIdToVrtmStateMap;
	}

	public static void setContainerIdToVrtmStateMap(Map<String, String> containerIdToVrtmStateMap) {
		DockerProxyCache.containerIdToVrtmStateMap = containerIdToVrtmStateMap;
	}

	public static String getImageIdForContainer(String containerId) {

		return containerIdToImageIdMap.get(containerId);
	}

	public static String setImageIdForContainer(String containerId, String imageId) {

		return containerIdToImageIdMap.put(containerId, imageId);
	}

	public static Map<String, String> getContainerIdToImageIdMap() {
		return containerIdToImageIdMap;
	}

	public static void setContainerIdToImageIdMap(Map<String, String> containerIdToImageIdMap) {
		DockerProxyCache.containerIdToImageIdMap = containerIdToImageIdMap;
	}

	public static org.slf4j.Logger getLog() {
		return log;
	}

	public static String getVrtmStateForContainerId(String containerId) {

		return containerIdToVrtmStateMap.get(containerId);
	}

	public static String setVrtmStateForContainerId(String containerId, String state) {

		return containerIdToVrtmStateMap.put(containerId, state);
	}

	public static String getEngineHost() {
		return engineHost;
	}

	public static void setEngineHost(String engineHost) {
		DockerProxyCache.engineHost = engineHost;
	}

	public static int getEnginePort() {
		return enginePort;
	}

	public static void setEnginePort(int enginePort) {
		DockerProxyCache.enginePort = enginePort;
	}

	public static String getEngineServerType() {
		return engineServerType;
	}

	public static void setEngineServerType(String engineServerType) {
		DockerProxyCache.engineServerType = engineServerType;
	}

	public static String getEngineSocket() {
		return engineSocket;
	}

	public static void setEngineSocket(String engineSocket) {
		DockerProxyCache.engineSocket = engineSocket;
	}

}
