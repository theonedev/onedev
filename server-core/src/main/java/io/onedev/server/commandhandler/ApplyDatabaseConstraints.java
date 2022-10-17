package io.onedev.server.commandhandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class ApplyDatabaseConstraints extends AbstractPlugin {

	public static final String COMMAND = "apply-db-constraints";
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyDatabaseConstraints.class);
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final DataManager dataManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public ApplyDatabaseConstraints(SessionFactoryManager sessionFactoryManager, DataManager dataManager, 
			HibernateConfig hibernateConfig) {
		this.sessionFactoryManager = sessionFactoryManager;
		this.dataManager = dataManager;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (OneDev.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before applying db constraints");
			System.exit(1);
		}

		sessionFactoryManager.start();
		
		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				dataManager.checkDataVersion(conn, false);
				
				logger.warn("This script is mainly used to apply database constraints after fixing database integrity issues (may happen during restore/upgrade)");
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				while (true) {
					logger.warn("Press 'y' to run the script, or 'n' to stop");
					String input;
					try {
						input = reader.readLine();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					if (input.equalsIgnoreCase("y"))
						break;
					else if (input.equalsIgnoreCase("n"))
						System.exit(0);
				}

				dataManager.dropConstraints(conn);
				dataManager.applyConstraints(conn);
				
				return null;
			}
			
		});

		if (hibernateConfig.isHSQLDialect()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		logger.info("Database constraints is applied successfully");
		
		System.exit(0);
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
