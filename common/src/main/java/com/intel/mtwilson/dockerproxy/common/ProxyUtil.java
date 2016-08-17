package com.intel.mtwilson.dockerproxy.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.intel.mtwilson.dockerproxy.exception.DockerProxyException;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;

public class ProxyUtil {


	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyUtil.class);

	public static String extractContainerIdformStopRequest(String requestUri) {
		Pattern pattern = Pattern.compile(RegularExpressions.STOP_REGEX);
		return getResultByPattern(pattern, requestUri);
	}

	public static String extractContainerIdformRestartRequest(String requestUri) {
		Pattern pattern = Pattern.compile(RegularExpressions.RESTART_REGEX);
		return getResultByPattern(pattern, requestUri);
	}

	public static String extractContainerIdformKillRequest(String requestUri) {
		Pattern pattern = Pattern.compile(RegularExpressions.KILL_REGEX);
		return getResultByPattern(pattern, requestUri);
	}

	public static String extractContainerIdformDeleteRequest(String requestUri) {
		//Pattern pattern = Pattern.compile("containers/(.*)\\?");
		Pattern pattern = Pattern.compile(RegularExpressions.DELETE_REGEX);
		return getResultByPattern(pattern, requestUri);
	}

	public static String extractContainerIdformStartRequest(String requestUri) {
		Pattern pattern = Pattern.compile(RegularExpressions.START_REGEX);
		return getResultByPattern(pattern, requestUri);
	}

	public static String getResultByPattern(Pattern pattern, String request) {
		Matcher matcher = pattern.matcher(request);
		String result = null;
		while (matcher.find()) {
			log.debug(matcher.group(1));
			result = matcher.group(1);
		}
		return result;

	}

	public static boolean checkPatternExists(Pattern pattern, String request) {
		Matcher matcher = pattern.matcher(request);
		boolean patternExists = false;
		if (matcher.find()) {
			patternExists = true;
		}		return patternExists;

	}

	public static String extractContainerIdFromCreateContainerResponse(String responseBody) {
		log.debug("extractContainerIdFromCreateContainerResponse,responseBody:" + responseBody);
		String containerId = null;
		int indexOfCurlyBranceOpen = responseBody.indexOf("{");
		if (indexOfCurlyBranceOpen == -1) {
			return null;
		}
		String body = responseBody.substring(indexOfCurlyBranceOpen);
		int indexOfCurlyBranceClose = body.lastIndexOf("}");
		if (indexOfCurlyBranceClose == -1) {
			return null;
		}
		String jsonString = body.substring(0, indexOfCurlyBranceClose + 1);
		log.debug("extractContainerIdFromCreateContainerResponse ,jsonString::" + jsonString);
		Map<String, Object> map = convertJsonToMap(jsonString);
		containerId = (String) map.get("Id");

		return containerId;
	}

	public static String getContainerNameFromContainerId(String containerId) {

		boolean isV2Version = false;
		FileInputStream fisTargetFile = null;
		try {
			File configJsonFile = new File(
					DockerProxyCache.getDockerInstallationPath() + "/containers/" + containerId + "/config.json");

			if (!configJsonFile.exists()) {
				log.debug("getContainerNameFromContainerId ,Reading from config.v2.json as config.json do not exist");
				configJsonFile = new File(DockerProxyCache.getDockerInstallationPath() + "/containers/" + containerId
						+ "/config.v2.json");
				isV2Version = true;
			}

			fisTargetFile = new FileInputStream(configJsonFile);
		} catch (FileNotFoundException e) {
			log.error("Unable to read config json, isV2Version:" + isV2Version);
		}

		String targetFileStr = null;
		try {
			targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
		} catch (IOException e) {
			log.error("Unable to read config json, isV2Version:" + isV2Version);

		}

		Map<String, Object> map = convertJsonToMap(targetFileStr);

		String containerName = (String) map.get("Name");

		return containerName;
	}

	public static String extractImageIdFromContainerId(String containerId) {

		log.debug("extractImageIdFromContainerId, Reading config.json from:: {} ",
				DockerProxyCache.dockerInstallationPath + "/containers/" + containerId + "/config.json");
		boolean isV2Version = false;
		FileInputStream fisTargetFile = null;
		try {
			File configJsonFile = new File(
					DockerProxyCache.getDockerInstallationPath() + "/containers/" + containerId + "/config.json");
			if (!configJsonFile.exists()) {
				log.info("extractImageIdFromContainerId reading from confi.v2.verison");
				configJsonFile = new File(DockerProxyCache.getDockerInstallationPath() + "/containers/" + containerId
						+ "/config.v2.json");
				isV2Version = true;
			}

			fisTargetFile = new FileInputStream(configJsonFile);
		} catch (FileNotFoundException e) {
			log.error("Unable to read config json, isV2Version: {}", isV2Version, e);
			return null;
		}

		String targetFileStr = null;
		try {
			targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
		} catch (IOException e) {
			log.error("Unable to read config json, isV2Version: {}", isV2Version);
			return null;
		}
		Map<String, Object> map = convertJsonToMap(targetFileStr);
		String imageId = null;
		String imageAttribute = (String) map.get("Image");
		log.trace("imageAttribute::" + imageAttribute);
		if (isV2Version) {
			imageId = imageAttribute.substring(imageAttribute.indexOf(":") + 1);
		} else {
			imageId = imageAttribute;
		}
		log.debug(" xtractImageIdFromContainerId, imageId::" + imageId);
		return imageId;
	}

	public static String extractImageIdFromCreateContainerRequest(String request) {
		log.debug("extractImageIdFromCreateContainerRequest , request::" + request);
		int indexOfCurlyBranceOpen = request.indexOf("{");

		String body = request.substring(indexOfCurlyBranceOpen);

		String jsonString = body.substring(0, body.lastIndexOf("}") + 1);
		Map<String, Object> map = convertJsonToMap(jsonString);

		String imageId = (String) map.get("Image");
		if (imageId.contains(":")) { // // Condition when docker repo:tag comes
			// // in Image attribute in json instead of
			// // imageId
			return null;
		}
		return imageId;
	}

	

	public static int executePolicyAgentCommands(String... args) throws DockerProxyException {
		int exitcode;
		if (args.length == 0) {
			log.error("Policyagent command needs at least one argument");
			return 1;
		}

		try {
			exitcode = executeCommandInExecUtil(DockerProxyCache.getPolicyAgentPath(), args);
		} catch (Exception e) {
			log.error("Error in executeDockerCommands docker " + args[0], e);
			throw new DockerProxyException("Error in executing policy agent", e);
		}
		return exitcode;

	}

	public static int executeCommandInExecUtil(String command, String... args) throws IOException {
		Result result = ExecUtil.execute(command, args);
		log.debug("######################  RESPONSE for Command :" + command + "\n\n  ######STDOUT::  "
				+ result.getStdout() + "\n\n Exit code::" + result.getExitCode());
		if (StringUtils.isNotBlank(result.getStderr())) {
			log.debug("###################### STDERR output:: " + result.getStderr());
		}
		return result.getExitCode();
	}

	public static boolean startDockerEngine(String serverType, String host, int port, String engineSocketFilePath)
			throws IOException {
		int status = -1;
		if (Constants.SERVER_TYPE_TCP.equalsIgnoreCase(serverType)) {
			status = executeCommandInExecUtil(Constants.DOCKER_COMMAND, "-d", "-H", host + ":" + port);
		} else if (Constants.SERVER_TYPE_UNIX.equalsIgnoreCase(serverType)) {
			status = executeCommandInExecUtil(Constants.DOCKER_COMMAND, "-d", "-H", "unix://" + engineSocketFilePath);
		}

		return (status == 0);
	}

	public static boolean policyAgentValidate(String containerId, String imageId) throws DockerProxyException {
		String containerName = getContainerNameFromContainerId(containerId);
		String rootfsMountPath;
		rootfsMountPath = Constants.MOUNT_PATH + containerId + "/rootfs";
		File deviceMapperMountPath = new File(rootfsMountPath);
		int result = -1;
		if (deviceMapperMountPath.exists()) { // / if image is mounted using
												// device mapper
			log.debug("Executing PolicyAgent command::: policyagent container_launch " + rootfsMountPath + " " + imageId
					+ " " + containerId + " " + containerName + " " + Constants.TRUST_POLICY_PATH);
			result = executePolicyAgentCommands("container_launch", rootfsMountPath, imageId, containerId,
					containerName, Constants.TRUST_POLICY_PATH);

		} else {
			log.debug("Executing PolicyAgent command for devicemapper::: policyagent container_launch "
					+ Constants.MOUNT_PATH + containerId + " " + imageId + " " + containerId + " " + containerName + " "
					+ Constants.TRUST_POLICY_PATH);
			result = executePolicyAgentCommands("container_launch", Constants.MOUNT_PATH + containerId, imageId,
					containerId, containerName, Constants.TRUST_POLICY_PATH);
		}
		if(result == 0){
			
			return true;
		}
			
		return false;
	}

	public static boolean isNoContentResponse(String response) {
		if (StringUtils.isNotBlank(response) && response.contains(Constants.RESPONSE_NO_CONTENT)) {
			return true;
		}
		return false;
	}

	public static boolean isNotModifiedResponse(String response) {
		if (StringUtils.isNotBlank(response) && response.contains(Constants.RESPONSE_NOT_MODIFIED)) {
			return true;
		}
		return false;
	}

	private static Map<String, Object> convertJsonToMap(String jsonString) {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			map = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonParseException e) {
			log.error("Error parsing json: ", jsonString, e);
		} catch (JsonMappingException e) {
			log.error("Error mapping json: ", jsonString, e);
		} catch (IOException e) {
			log.error("Error getting container id from  json: ", jsonString, e);
		}
		return map;
	}
}
