package com.intel.mtwilson.dockerproxy.common;

public class RegularExpressions {
	
	public static final String START_REGEX = "containers/(.*?)/start";
	public static final String CONTAINER_CREATE = "containers/create";
	public static final String DELETE_REGEX = "containers/(.*?)$";
	public static final String KILL_REGEX = "containers/(.*?)/kill";
	public static final String STOP_REGEX = "containers/(.*?)/stop";
	public static final String RESTART_REGEX = "containers/(.*?)/restart";

}
