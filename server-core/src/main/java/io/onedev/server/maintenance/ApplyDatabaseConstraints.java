package io.onedev.server.maintenance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class ApplyDatabaseConstraints extends DefaultPersistManager {

	public static final String COMMAND = "apply-db-constraints";
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyDatabaseConstraints.class);
	
	@Inject
	public ApplyDatabaseConstraints(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator,
			TransactionManager transactionManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator, transactionManager);
	}

	@Override
	public void start() {
		if (Bootstrap.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before applying db constraints");
			System.exit(1);
		}
		checkDataVersion(false);
		
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

		Metadata metadata = buildMetadata();
		dropConstraints(metadata);
		applyConstraints(metadata);

		if (getDialect().toLowerCase().contains("hsql")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		logger.info("Database constraints is applied successfully");
		
		System.exit(0);
	}

	@Override
	public SessionFactory getSessionFactory() {
		throw new UnsupportedOperationException();
	}

}
