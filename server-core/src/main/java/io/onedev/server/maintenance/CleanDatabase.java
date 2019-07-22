package io.onedev.server.maintenance;

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
public class CleanDatabase extends DefaultPersistManager {

	public static final String COMMAND = "clean-db";
	
	private static final Logger logger = LoggerFactory.getLogger(CleanDatabase.class);
	
	@Inject
	public CleanDatabase(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, 
			TransactionManager transactionManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator, transactionManager);
	}

	@Override
	public void start() {
		if (Bootstrap.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before cleaning database");
			System.exit(1);
		}
		checkDataVersion(false);

		Metadata metadata = buildMetadata();
		cleanDatabase(metadata);

		if (getDialect().toLowerCase().contains("hsql")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("Database is cleaned successfully");
		
		System.exit(0);
	}

	@Override
	public SessionFactory getSessionFactory() {
		throw new UnsupportedOperationException();
	}

}
