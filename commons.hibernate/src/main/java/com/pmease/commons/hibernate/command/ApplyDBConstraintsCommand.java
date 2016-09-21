package com.pmease.commons.hibernate.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
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
import com.pmease.commons.hibernate.IdManager;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.migration.Migrator;

@Singleton
public class ApplyDBConstraintsCommand extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(ApplyDBConstraintsCommand.class);
	
	@Inject
	public ApplyDBConstraintsCommand(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			@Named("hibernate") Properties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao, Validator validator) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao, validator);
	}

	@Override
	public void start() {
		if (Bootstrap.getServerRunningFile().exists()) {
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
