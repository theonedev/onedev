package io.onedev.server.oem;

import io.onedev.commons.bootstrap.Bootstrap;
import jakarta.inject.Singleton;

import java.io.File;
import java.util.Properties;

import static io.onedev.commons.utils.FileUtils.loadProperties;
import static org.apache.commons.lang3.StringUtils.stripEnd;

@Singleton
public class DefaultOemService implements OemService {

	private final Properties properties;

	public DefaultOemService() {
		var file = new File(Bootstrap.installDir, "oem/oem.properties");
		properties = file.exists() ? loadProperties(file) : new Properties();
	}

	@Override
	public String getProductName() {
		return properties.getProperty("product_name", "OneDev");
	}

	@Override
	public String getWebsite() {
		return stripEnd(properties.getProperty("website", DEFAULT_WEBSITE), "/");
	}

	@Override
	public String getDocsite() {
		return stripEnd(properties.getProperty("docsite", DEFAULT_DOCSITE), "/");
	}

}
