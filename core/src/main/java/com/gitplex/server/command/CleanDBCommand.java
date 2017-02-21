package com.gitplex.server.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.server.persistence.DefaultPersistManager;
import com.gitplex.server.persistence.HibernateProperties;
import com.gitplex.server.persistence.IdManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.util.validation.EntityValidator;

@Singleton
public class CleanDBCommand extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(CleanDBCommand.class);
	
	@Inject
	public CleanDBCommand(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator);
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
