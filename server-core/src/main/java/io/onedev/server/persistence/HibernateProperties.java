package io.onedev.server.persistence;

import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Environment;

public class HibernateProperties extends Properties {

	private static final long serialVersionUID = 1L;

	public HibernateProperties(Properties properties) {
		for (Map.Entry<Object, Object> entry: properties.entrySet()) 
			put(entry.getKey(), entry.getValue());
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
	
}
