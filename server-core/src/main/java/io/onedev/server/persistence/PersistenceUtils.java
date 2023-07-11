package io.onedev.server.persistence;

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
import static java.lang.String.format;

public class PersistenceUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(PersistenceUtils.class);
	
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
			try (ResultSet resultset = metadata.getTables(conn.getCatalog(), conn.getSchema(), tableName, null)) {
				if (resultset.next())
					return true;
			}
			try (ResultSet resultset = metadata.getTables(conn.getCatalog(), conn.getSchema(), tableName.toUpperCase(), null)) {
				if (resultset.next())
					return true;
			}
			try (ResultSet resultset = metadata.getTables(conn.getCatalog(), conn.getSchema(), tableName.toLowerCase(), null)) {
				if (resultset.next())
					return true;
			}
			return false;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T callWithLock(Connection conn, Callable<T> callable) {
		var tableName = "o_DatabaseLock";
		callWithTransaction(conn, () -> {
			if (!tableExists(conn, tableName)) {
				try (var stmt = conn.createStatement()) {
					stmt.execute(format("create table %s (o_id int)", tableName));
					stmt.executeUpdate(format("insert into %s values(1)", tableName));
				}
			}
			return null;
		});
		return callWithTransaction(conn, () -> {
			while (true) {
				try (var stmt = conn.createStatement()) {
					stmt.executeQuery(format("select * from %s for update", tableName));
					break;
				} catch (Exception e) {
					logger.warn("Unable to get database lock, will retry", e);
					Thread.sleep(5000);
				}
			}
			return callable.call();
		});
	}	
	
}
