package com.intel.mtwilson.dockerproxy.api;

import java.util.Map;

public class DockerEngineBaseRequestResponse {
	public String RequestMethod;
	public String RequestUri;
	public byte[] RequestBody;
	public Map<String,String> RequestHeaders;
}
