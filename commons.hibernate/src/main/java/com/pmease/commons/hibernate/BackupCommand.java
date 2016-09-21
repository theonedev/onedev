package com.pmease.commons.hibernate;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.hibernate.Interceptor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.bootstrap.BootstrapUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.migration.Migrator;
import com.pmease.commons.util.FileUtils;

@Singleton
public class BackupCommand extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(BackupCommand.class);
	
	@Inject
	public BackupCommand(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			@Named("hibernate") Properties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao, Validator validator) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao, validator);
	}

	@Override
	public void start() {
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing backup file parameter. Usage: {} <path to database backup file>", Bootstrap.command.getScript());
			System.exit(1);
		}
		File backupFile = new File(Bootstrap.command.getArgs()[0]);
		if (!backupFile.isAbsolute())
			backupFile = new File(Bootstrap.getBinDir(), backupFile.getPath());
		
		if (backupFile.exists()) {
			logger.error("Backup file already exists: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
		
		checkDataVersion(false);
		
		logger.info("Backing up database to {}...", backupFile.getAbsolutePath());
		
		Metadata metadata = buildMetadata();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();

		File tempDir = BootstrapUtils.createTempDir("backup");
		try {
			exportData(tempDir);
			BootstrapUtils.zip(tempDir, backupFile);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		} finally {
			FileUtils.deleteDir(tempDir);
		}

		sessionFactory.close();
		logger.info("Database is successfully backed up to {}", backupFile.getAbsolutePath());
		
		System.exit(0);
	}

}
