package io.onedev.server.persistence;

import io.onedev.commons.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Callable;

import static io.onedev.commons.utils.ExceptionUtils.unchecked;
import static io.onedev.commons.utils.FileUtils.loadProperties;
import static java.lang.String.format;
import static org.hibernate.cfg.AvailableSettings.*;

public class PersistenceUtils {

	private static final Logger logger = LoggerFactory.getLogger(PersistenceUtils.class);
	
	private static final String[] HIBERNATE_PROPS = new String[] {
			DIALECT, DRIVER, URL, USER, PASS, "hibernate.hikari.leakDetectionThreshold",
			"hibernate.hikari.maxLifetime", "hibernate.hikari.connectionTimeout",
			"hibernate.hikari.maximumPoolSize", "hibernate.hikari.validationTimeout",
			"hibernate.show_sql"
	};
	
	public static HibernateConfig loadHibernateConfig(File installDir) {
		File file = new File(installDir, "conf/hibernate.properties");
		HibernateConfig hibernateConfig = new HibernateConfig(loadProperties(file));
		String url = hibernateConfig.getProperty(URL);
		hibernateConfig.setProperty(URL,
				io.onedev.commons.utils.StringUtils.replace(url, "${installDir}", installDir.getAbsolutePath()));

		for (String prop: HIBERNATE_PROPS) {
			String env = System.getenv(prop.replace('.', '_'));
			if (env != null)
				hibernateConfig.setProperty(prop, env);
		}

		return hibernateConfig;
	}

	public static Connection openConnection(HibernateConfig hibernateConfig, ClassLoader classLoader) {
		try {
			Driver driver = (Driver) Class.forName(hibernateConfig.getDriver(), true, classLoader).getDeclaredConstructor().newInstance();
			Properties connectProps = new Properties();
			String user = hibernateConfig.getUser();
			String password = hibernateConfig.getPassword();
			if (user != null)
				connectProps.put("user", user);
			if (password != null)
				connectProps.put("password", password);

			return driver.connect(hibernateConfig.getUrl(), connectProps);
		} catch (Exception e) {
			throw unchecked(e);
		}
	}

	public static <T> T callWithTransaction(Connection conn, Callable<T> callable) {
		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			try {
				T result = callable.call();
				conn.commit();
				return result;
			} catch (Exception e) {
				conn.rollback();
				throw unchecked(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ClassLoader newSiteLibClassLoader(File installDir) {
		var siteLibs = new ArrayList<URL>();
		for (var file: new File(installDir, "site/lib").listFiles()) {
			if (file.getName().endsWith(".jar")) {
				try {
					siteLibs.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return new URLClassLoader(siteLibs.toArray(new URL[0]), 
				PersistenceUtils.class.getClassLoader());
	}
	
	public static boolean tableExists(Connection conn, String tableName) {
		try {
			var metadata = conn.getMetaData();
			try (ResultSet resultset = metadata.getTables(null, null, tableName, null)) {
				if (resultset.next())
					return true;
			}
			try (ResultSet resultset = metadata.getTables(null, null, tableName.toUpperCase(), null)) {
				if (resultset.next())
					return true;
			}
			try (ResultSet resultset = metadata.getTables(null, null, tableName.toLowerCase(), null)) {
				if (resultset.next())
					return true;
			}
			return false;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T callWithDatabaseLock(Connection conn, Callable<T> callable) {
		var lockTableName = "o_DatabaseLock";
		callWithTransaction(conn, () -> {
			try (var stmt = conn.createStatement()) {
				stmt.executeQuery(format("select * from o_ModelVersion for update"));
			}
			if (!tableExists(conn, lockTableName)) {
				try (var stmt = conn.createStatement()) {
					stmt.execute(format("create table %s (o_id int)", lockTableName));
					stmt.executeUpdate(format("insert into %s values(1)", lockTableName));
				}
			}
			return null;
		});
		return callWithTransaction(conn, () -> {
			while (true) {
				try (var stmt = conn.createStatement()) {
					stmt.executeQuery(format("select * from %s for update", lockTableName));
					break;
				} catch (Exception e) {
					Thread.sleep(1000);
				}
			}
			return callable.call();
		});
	}	
	
}
