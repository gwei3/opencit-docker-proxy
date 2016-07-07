package com.intel.mtwilson.dockerproxy.common;

import java.io.File;
import java.io.IOException;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;

public class DockerProxyConfigUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerProxyConfigUtil.class);
	private static Configuration loadedConfiguration;
	static {
		File hubPropertiesFile = new File(
				Folders.configuration() + File.separator + Constants.DOCKER_PROXY_PROPERTIES_FILE_NAME);
		ConfigurationProvider provider;

		try {
			provider = ConfigurationFactory.createConfigurationProvider(hubPropertiesFile);
			loadedConfiguration = provider.load();
		} catch (IOException e) {
			log.error("Unable to read config file");
		}
	}

	public static String get(String key) {
		if (loadedConfiguration == null) {
			return null;
		}
		return loadedConfiguration.get(key);
	}

	public static String get(String key, String defaultValue) {
		if (loadedConfiguration == null) {
			return null;
		}
		return loadedConfiguration.get(key, defaultValue);
	}

}
