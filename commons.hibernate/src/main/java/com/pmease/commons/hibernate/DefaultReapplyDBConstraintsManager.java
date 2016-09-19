package com.pmease.commons.hibernate;

import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.migration.MigrationHelper;
import com.pmease.commons.hibernate.migration.Migrator;

@Singleton
public class DefaultReapplyDBConstraintsManager extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultReapplyDBConstraintsManager.class);
	
	@Inject
	public DefaultReapplyDBConstraintsManager(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			@Named("hibernate") Properties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao);
	}

	@Override
	public void start() {
		if (Bootstrap.getServerRunningFile().exists()) {
			logger.error("Please stop server before reapply db constraints");
			System.exit(1);
		}
		String dbDataVersion = readDbDataVersion();
		if (dbDataVersion == null) {
			logger.error("Database is not populated yet");
			System.exit(1);
		}
		String appDataVersion = MigrationHelper.getVersion(migrator.getClass());
		if (dbDataVersion != null && !dbDataVersion.equals(appDataVersion)) {
			logger.error("Can not apply constraints to a database populated by a different program version");
			System.exit(1);
		}

		Metadata metadata = buildMetadata();
		dropForeignKeyConstraints(metadata);
		applyForeignKeyConstraints(metadata);

		logger.info("Database constraints is applied successfully");
		
		System.exit(0);
	}

	@Override
	public SessionFactory getSessionFactory() {
		throw new UnsupportedOperationException();
	}

}
