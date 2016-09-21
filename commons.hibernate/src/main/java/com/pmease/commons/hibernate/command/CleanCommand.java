package com.pmease.commons.hibernate.command;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.DefaultPersistManager;
import com.pmease.commons.hibernate.HibernateProperties;
import com.pmease.commons.hibernate.IdManager;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.migration.Migrator;

@Singleton
public class CleanCommand extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(CleanCommand.class);
	
	@Inject
	public CleanCommand(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao, Validator validator) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao, validator);
	}

	@Override
	public void start() {
		if (Bootstrap.getServerRunningFile().exists()) {
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
