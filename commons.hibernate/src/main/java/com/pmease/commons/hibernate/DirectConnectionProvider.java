package com.pmease.commons.hibernate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.ExceptionUtils;

@Singleton
@SuppressWarnings("serial")
public class DirectConnectionProvider implements ConnectionProvider {

	public void close() throws HibernateException {

	}

	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}

	public void configure(Properties props) throws HibernateException {

	}

	public Connection getConnection() throws SQLException {
		Properties props = AppLoader.injector.getInstance(Key.get(Properties.class, Names.named("hibernate")));
		try {
			Class.forName(props.getProperty(AvailableSettings.DRIVER));
	    	Connection conn = DriverManager.getConnection(
	    			props.getProperty(AvailableSettings.URL), 
	    			props.getProperty(AvailableSettings.USER), 
	    			props.getProperty(AvailableSettings.PASS));
	    	conn.setAutoCommit(true);
	    	return conn;
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	public boolean supportsAggressiveRelease() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return DirectConnectionProvider.class.equals(unwrapType);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> T unwrap(Class<T> unwrapType) {
		if (isUnwrappableAs(unwrapType)) {
			return (T) this;
		} else {
			throw new UnknownUnwrapTypeException(unwrapType);
		}
	}

}
