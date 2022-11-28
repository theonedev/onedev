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
public class CleanDatabase extends AbstractPlugin {

	public static final String COMMAND = "clean-db";
	
	private static final Logger logger = LoggerFactory.getLogger(CleanDatabase.class);
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final DataManager dataManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public CleanDatabase(SessionFactoryManager sessionFactoryManager, DataManager dataManager, 
			HibernateConfig hibernateConfig) {
		this.sessionFactoryManager = sessionFactoryManager;
		this.dataManager = dataManager;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (OneDev.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before cleaning database");
			System.exit(1);
		}

		sessionFactoryManager.start();
		
		// Run this in autocommit mode as some sqls in the clean script may fail
		// when drop non-existent constraints, and we want to ignore them and 
		// continue to execute other sql statements without rolling back whole 
		// transaction
		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				dataManager.checkDataVersion(conn, false);
				dataManager.cleanDatabase(conn);
				return null;
			}
			
		}, true);

		if (hibernateConfig.isHSQLDialect()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("Database is cleaned successfully");
		
		System.exit(0);
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
