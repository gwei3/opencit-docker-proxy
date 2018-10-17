package com.intel.mtwilson.dockerproxy.api;

import java.util.Map;

public class DockerEngineResponse extends DockerEngineBaseRequestResponse {
	public byte[] ResponseBody;
	public int ResponseStatusCode;
	public Map<String,String> ResponseHeaders;
}
