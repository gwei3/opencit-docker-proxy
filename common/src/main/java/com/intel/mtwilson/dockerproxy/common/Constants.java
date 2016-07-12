package com.intel.mtwilson.dockerproxy.common;

public class Constants {
    public static final int DEFAULT_LISTEN_PORT = 9999;
    public static final int DEFAULT_MAX_THREADS = 100; // Max no of thread
						       // supported.

    public static final String SERVER_TYPE = "docker.proxy.server.type";
    public static final String SERVER_TYPE_TCP = "tcp";
    public static final String SERVER_TYPE_UNIX = "unix";
    public static final String SERVER_LISTEN_PORT = "docker.proxy.listen.port";

    public static final String ENGINE_SERVER_TYPE = "docker.engine.server.type";
    public static final String DOCKER_CONF_FILE_PATH = "docker.conf.file.path";

    public static final String ENGINE_SERVER_LISTEN_PORT = "docker.engine.listen.port";
    public static final String ENGINE_SERVER_HOST = "docker.engine.host";
    public static final String mountDockerScript = "/opt/docker-proxy/bin/mount_docker_script.sh";
    public static final String POLICY_AGENT_COMMAND = "policyagent";
    public static final String DOCKER_COMMAND = "docker";
    public static final String MOUNT_PATH = "/mnt/images/";
    public static final String ENGINE_SOCKET_FILE_PATH = "docker.engine.socket.file";
    public static final String HOST = "localhost";
    public static final String DOCKER_INSTALLATION_PATH = "docker.installation.path";
    public static final String POLICY_AGENT_PATH = "policy.agent.path";
    public static final String DEFAULT_POLICY_AGENT_PATH = "/usr/local/bin/policyagent";
    public static final String VRTM_PORT = "vrtm.port";

    public static final String DEFAULT_DOCKER_INSTALL_PATH = "/var/lib/docker";
    public static final String DEFAULT_VRTM_PORT = "16005";
    public static final String DEFAULT_DOCKER_SOCKET = "/var/run/docker.sock";
    public static final String DEFAULT_DOCKER_CONFIG_PATH = "/etc/default/docker";
    public static final String DEFAULT_DOCKER_OPT_STRING = "-H unix:///var/run/docker.sock";
    public static final String VRTM_STATUS_STOPPED = "2";
    public static final String VRTM_STATUS_STARTED = "1";
    public static final String VRTM_STATUS_DELETED = "3";
    public static final int VRTM_CONTAINER_STATUS_INDEX = 25;
    public static final String LOCALHOST = "localhost";
    public static final String METHODNAME_CONTAINER_UPDATE_STATUS = "set_vm_uuid";
    public static final String TRUST_POLICY_PATH = "/trust/trustpolicy.xml";
    public static final String DEVICE_MAPPER_TRUST_POLICY_PATH = "/rootfs/trust/trustpolicy.xml";
    public static final String HEADER_END_MARKER="\r\n\r\n";
    public static final String RESPONSE_NO_CONTENT = "204 No Content";
    public static final String RESPONSE_NOT_MODIFIED = "304 Not Modified";
    public static final String TLS_CA_CERT = "--tlscacert";
    public static final String SPACE = " ";
    public static final String TLS_CERT = "--tlscert";
    public static final String TLS_KEY = "--tlskey";
    public static String EMPTY = "";
	public static final String END_OF_EACH_HEADER = "\r\n";
	public static final String END_OF_HEADER = "\r\n\r\n";
    public static final String MEDIATYPE_DOCKER_PLUGIN_v1 = "application/vnd.docker.plugins.v1+json";
    public static final String MEDIATYPE_DOCKER_PLUGIN_v1_1 = "application/vnd.docker.plugins.v1.1+json";
    public static final String MEDIATYPE_DOCKER_PLUGIN_v1_2 = "application/vnd.docker.plugins.v1.2+json";
	public static final String DOCKER_PROXY_PROPERTIES_FILE_NAME = "docker-proxy.properties";
	public static String POST = "POST";
	public static String DELETE = "DELETE";
	}
