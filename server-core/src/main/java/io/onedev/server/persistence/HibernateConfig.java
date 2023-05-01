package io.onedev.server.persistence;

import java.io.File;
import java.util.Properties;

import com.google.common.hash.Hashing;
import io.onedev.commons.utils.StringUtils;
import org.hibernate.cfg.Environment;

import static io.onedev.commons.utils.FileUtils.loadProperties;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hibernate.cfg.AvailableSettings.*;

public class HibernateConfig extends Properties {

	private static final long serialVersionUID = 1L;

	private static final String[] ENVS = new String[] {
			DIALECT, DRIVER, URL, USER, PASS, "hibernate.hikari.leakDetectionThreshold",
			"hibernate.hikari.maxLifetime", "hibernate.hikari.connectionTimeout",
			"hibernate.hikari.maximumPoolSize", "hibernate.hikari.validationTimeout",
			"hibernate.show_sql"
	};
	
	private volatile String clusterCredential; 

	public HibernateConfig(File installDir) {
		File file = new File(installDir, "conf/hibernate.properties");
		putAll(loadProperties(file));
		put("hibernate.cache.hazelcast.shutdown_on_session_factory_close", "false");
		
		String url = getProperty(URL);
		setProperty(URL, StringUtils.replace(url, "${installDir}", installDir.getAbsolutePath()));

		for (String env: ENVS) {
			String value = System.getenv(env.replace('.', '_'));
			if (value != null)
				setProperty(env, value);
		}
	}

	public String getDialect() {
		return getProperty(Environment.DIALECT);
	}

	public String getDriver() {
		return getProperty(Environment.DRIVER);
	}

	public String getUrl() {
		return getProperty(Environment.URL);
	}

	public String getUser() {
		return getProperty(Environment.USER);
	}

	public String getPassword() {
		return getProperty(Environment.PASS);
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
	
	public String getClusterCredential() {
		if (clusterCredential == null) {
			var dbPassword = getPassword();
			if (dbPassword == null)
				dbPassword = "";
			clusterCredential = Hashing.sha256().hashString(dbPassword, UTF_8).toString();
		}
		return clusterCredential;
	}
	
}
