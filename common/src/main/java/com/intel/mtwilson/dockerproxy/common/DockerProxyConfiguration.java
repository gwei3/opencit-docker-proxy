package com.intel.mtwilson.dockerproxy.common;

public class DockerProxyConfiguration {

    private String engineHost;
    private int enginePort;
    private String engineHostParameter;
    private String engineServerType;
    private String engineSocket;
    private String proxyHost;
    private String proxySocket;
    private int proxyPort;
    private String proxyServerType;
    private String proxyHostParameter;
    private String dockerInstallationPath;
    private int vrtmPort;

    public int getVrtmPort() {
	return vrtmPort;
    }

    public void setVrtmPort(int vrtmPort) {
	this.vrtmPort = vrtmPort;
    }

    public String getDockerInstallationPath() {
	return dockerInstallationPath;
    }

    public void setDockerInstallationPath(String dockerInstallationPath) {
	this.dockerInstallationPath = dockerInstallationPath;
    }

    public String getEngineSocket() {
	return engineSocket;
    }

    public void setEngineSocket(String engineSocket) {
	this.engineSocket = engineSocket;
    }

    public String getProxySocket() {
	return proxySocket;
    }

    public void setProxySocket(String proxySocket) {
	this.proxySocket = proxySocket;
    }

    public String getEngineServerType() {
	return engineServerType;
    }

    public void setEngineServerType(String engineServerType) {
	this.engineServerType = engineServerType;
    }

    public String getProxyServerType() {
	return proxyServerType;
    }

    public void setProxyServerType(String proxyServerType) {
	this.proxyServerType = proxyServerType;
    }

    public String getEngineHost() {
	return engineHost;
    }

    public void setEngineHost(String engineHost) {
	this.engineHost = engineHost;
    }

    public int getEnginePort() {
	return enginePort;
    }

    public void setEnginePort(int enginePort) {
	this.enginePort = enginePort;
    }

    public String getEngineHostParameter() {
	return engineHostParameter;
    }

    public void setEngineHostParameter(String engineHostParameter) {
	this.engineHostParameter = engineHostParameter;
    }

    public String getProxyHost() {
	return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
	this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
	return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
	this.proxyPort = proxyPort;
    }

    public String getProxyHostParameter() {
	return proxyHostParameter;
    }

    public void setProxyHostParameter(String proxyHostParameter) {
	this.proxyHostParameter = proxyHostParameter;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder("DockerProxyConfiguration [");
	builder.append("engineHost=" + engineHost);
	builder.append(", enginePort=" + enginePort);
	builder.append(", engineHostParameter=" + engineHostParameter);
	builder.append(", engineServerType=" + engineServerType);
	builder.append(", proxyServerType=" + proxyServerType);
	builder.append(", engineSocket=" + engineSocket);
	builder.append(", proxyHost=" + proxyHost);
	builder.append(", proxySocket=" + proxySocket);
	builder.append(", proxyPort=" + proxyPort);
	builder.append(", proxyHostParameter=" + proxyHostParameter);
	builder.append(", dockerInstallationPath=" + dockerInstallationPath);
	builder.append("]");

	return builder.toString();
    }

}
