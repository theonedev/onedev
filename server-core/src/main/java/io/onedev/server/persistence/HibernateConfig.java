package io.onedev.server.persistence;

import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Environment;

public class HibernateConfig extends Properties {

	private static final long serialVersionUID = 1L;

	public HibernateConfig(Properties properties) {
		for (Map.Entry<Object, Object> entry: properties.entrySet()) 
			put(entry.getKey(), entry.getValue());
		put("hibernate.cache.hazelcast.shutdown_on_session_factory_close", "false");
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
	
}
