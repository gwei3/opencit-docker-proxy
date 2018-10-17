package com.intel.mtwilson.dockerproxy.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUriFilter {



	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestUriFilter.class);

	public static boolean isCreateContainerRequest(String requestMethod, String requestUri) {
		boolean isCreateContainerRequest = true;
		// POST /v1.18/containers/create HTTP/1.1
		if (!requestMethod.equals(Constants.POST)) {
			isCreateContainerRequest = false;
		}
		if (!requestUri.contains(RegularExpressions.CONTAINER_CREATE)) {
			isCreateContainerRequest = false;
		}

		return isCreateContainerRequest;
	}

	public static boolean isRestartRequest(String requestMethod, String requestUri) {
		boolean isRestartRequest = true;

		if (!requestMethod.equals(Constants.POST)) {
			isRestartRequest = false;
		}
		if (!requestUri.contains("/restart")) {
			isRestartRequest = false;
		}

		return isRestartRequest;
	}

	public static boolean isStopRequest(String requestMethod, String requestUri) {

		if (!requestMethod.equals(Constants.POST)) {
			return false;
		}
		Pattern pattern = Pattern.compile(RegularExpressions.STOP_REGEX);
		return checkPatternExists(pattern, requestUri);

	}

	public static boolean isDeleteRequest(String requestMethod, String requestUri) {

		if (!requestMethod.equals(Constants.DELETE)) {
			return false;
		}
		Pattern pattern = Pattern.compile(RegularExpressions.DELETE_REGEX);
		return checkPatternExists(pattern, requestUri);

	}

	public static boolean isKillRequest(String requestMethod, String request) {

		if (!request.equals(Constants.POST)) {
			return false;
		}
		Pattern pattern = Pattern.compile(RegularExpressions.KILL_REGEX);
		return checkPatternExists(pattern, request);
	}

	public static boolean isStartRequest(String requestMethod, String requestUri) {
		if (!requestMethod.equals(Constants.POST)) {
			return false;
		}
		log.debug("isStartRequest ,requestUri :{}", requestUri);
		Pattern pattern = Pattern.compile(RegularExpressions.START_REGEX);
		return checkPatternExists(pattern, requestUri);

	}

	public static boolean checkPatternExists(Pattern pattern, String request) {
		Matcher matcher = pattern.matcher(request);
		boolean patternExists = false;
		if (matcher.find()) {
			patternExists = true;
		}
		return patternExists;

	}
}
