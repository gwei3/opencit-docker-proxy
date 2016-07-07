package com.intel.mtwilson.dockerproxy.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginResponse {
	@JsonProperty("Allow")
	public boolean allow;
	@JsonProperty("Msg")
	public String msg;
	@JsonProperty("Error")
	public String error;

	public PluginResponse(boolean allow, String msg, String error) {
		super();
		this.allow = allow;
		this.msg = msg;
		this.error = error;
	}

	public static PluginResponse CreateDenyPluginResponse(String denyMessage) {
		PluginResponse pluginResponse = new PluginResponse(false, null, denyMessage);
		return pluginResponse;
	}

	public boolean isAllow() {
		return allow;
	}

	public void setAllow(boolean allow) {
		this.allow = allow;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
