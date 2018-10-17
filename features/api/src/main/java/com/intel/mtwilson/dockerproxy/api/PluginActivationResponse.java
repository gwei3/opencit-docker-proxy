package com.intel.mtwilson.dockerproxy.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginActivationResponse {
    @JsonProperty("Implements")
	public List<String> pares_Implements;
	
}
