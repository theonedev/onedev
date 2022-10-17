package io.onedev.server.commandhandler;

import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.server.OneDev;
import io.onedev.server.persistence.ConnectionCallable;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class CheckDataVersion extends AbstractPlugin {

	public static final String COMMAND = "check-data-version";
	
	private static final Logger logger = LoggerFactory.getLogger(CheckDataVersion.class);

	private final SessionFactoryManager sessionFactoryManager;
	
	private final DataManager dataManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public CheckDataVersion(SessionFactoryManager sessionFactoryManager, DataManager dataManager, 
			HibernateConfig hibernateConfig) {
		this.sessionFactoryManager = sessionFactoryManager;
		this.dataManager = dataManager;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (OneDev.isServerRunning(Bootstrap.installDir) && hibernateConfig.isHSQLDialect()) {
			logger.error("Please stop server before checking data version");
			System.exit(1);
		}
		
		sessionFactoryManager.start();
		
		// Use system.out in case logger is suppressed by user as this output is important to 
		// upgrade procedure
		String dataVersion = dataManager.callWithConnection(new ConnectionCallable<String>() {

			@Override
			public String call(Connection conn) {
				return dataManager.checkDataVersion(conn, false);
			}
			
		});
		System.out.println("Data version: " + dataVersion);
		System.exit(0);
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
