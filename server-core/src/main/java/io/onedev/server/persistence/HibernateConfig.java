package io.onedev.server.persistence;

import static io.onedev.commons.utils.FileUtils.loadProperties;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_DRIVER;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_PASSWORD;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_URL;
import static org.hibernate.cfg.AvailableSettings.JAKARTA_JDBC_USER;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.hibernate.cfg.Environment;

public class HibernateConfig extends Properties {

	private static final long serialVersionUID = 1L;

	private static final String ENV_PASS_FILE ="hibernate_connection_password_file";
	
	// Existing installations and deployment manifests still use these OneDev configuration keys.
	private static final Map<String, String> LEGACY_CONNECTION_SETTINGS = Map.of(
			"hibernate.connection.driver_class", JAKARTA_JDBC_DRIVER,
			"hibernate.connection.url", JAKARTA_JDBC_URL,
			"hibernate.connection.username", JAKARTA_JDBC_USER,
			"hibernate.connection.password", JAKARTA_JDBC_PASSWORD);

	private static final String[] ENVS = new String[] {
			DIALECT, JAKARTA_JDBC_DRIVER, JAKARTA_JDBC_URL, JAKARTA_JDBC_USER, JAKARTA_JDBC_PASSWORD, "hibernate.hikari.leakDetectionThreshold",
			"hibernate.hikari.maxLifetime", "hibernate.hikari.connectionTimeout",
			"hibernate.hikari.maximumPoolSize", "hibernate.hikari.validationTimeout",
			"hibernate.show_sql", "hibernate.query.plan_cache_max_size",
			"hibernate.query.plan_parameter_metadata_max_size"
	};
	
	public HibernateConfig(File installDir) {
		File file = new File(installDir, "conf/hibernate.properties");
		putAll(loadProperties(file));
		LEGACY_CONNECTION_SETTINGS.forEach((legacy, current) -> {
			Object value = remove(legacy);
			if (value != null)
				putIfAbsent(current, value);
		});
		if ("org.hibernate.dialect.MySQL5InnoDBDialect".equals(getProperty(DIALECT)))
			put(DIALECT, "org.hibernate.dialect.MySQLDialect");
		
		String value = System.getenv(ENV_PASS_FILE);
		if (value != null) {
			try {
				setProperty(JAKARTA_JDBC_PASSWORD, FileUtils.readFileToString(new File(value), UTF_8).trim());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		LEGACY_CONNECTION_SETTINGS.forEach((legacy, current) -> {
			String override = System.getenv(legacy.replace('.', '_'));
			if (override != null)
				setProperty(current, override);
		});
		for (String env: ENVS) {
			value = System.getenv(env.replace('.', '_'));
			if (value != null)
				setProperty(env, value);
		}
		
		String url = getProperty(JAKARTA_JDBC_URL);
		url = Strings.CS.replace(url, "${installDir}", installDir.getAbsolutePath());
		setProperty(JAKARTA_JDBC_URL, url);
	}

	public String getDialect() {
		return getProperty(Environment.DIALECT);
	}

	public String getDriver() {
		return getProperty(Environment.JAKARTA_JDBC_DRIVER);
	}

	public String getUrl() {
		return getProperty(Environment.JAKARTA_JDBC_URL);
	}

	public String getUser() {
		return getProperty(Environment.JAKARTA_JDBC_USER);
	}

	public String getPassword() {
		return getProperty(Environment.JAKARTA_JDBC_PASSWORD);
	}
	
	public static boolean isHSQLDialect(String dialect) {
		return dialect.trim().equals("org.hibernate.dialect.HSQLDialect");
	}
	
	public static boolean isMySQLDialect(String dialect) {
		return dialect.toLowerCase().contains("mysql");		
	}
	
	public boolean isHSQLDialect() {
		return isHSQLDialect(getDialect());
	}
	
	public boolean isMySQLDialect() {
		return isMySQLDialect(getDialect());
	}
		
}
