package com.pmease.commons.hibernate;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.bootstrap.BootstrapUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.migration.MigrationHelper;
import com.pmease.commons.hibernate.migration.Migrator;
import com.pmease.commons.util.FileUtils;

@Singleton
public class DefaultRestoreManager extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRestoreManager.class);
	
	@Inject
	public DefaultRestoreManager(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			@Named("hibernate") Properties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao);
	}

	@Override
	public void start() {
		if (Bootstrap.args.length < 2) {
			logger.error("Missing backup file parameter. Usage: {} <path to database backup file>", Bootstrap.getCommandFile());
			System.exit(1);
		}
		File backupFile = new File(Bootstrap.args[1]);
		if (!backupFile.isAbsolute())
			backupFile = new File(Bootstrap.getBinDir(), backupFile.getPath());
		
		if (!backupFile.exists()) {
			logger.error("Unable to find file {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
				
		if (Bootstrap.getServerRunningFile().exists()) {
			logger.error("Please stop server before restore");
			System.exit(1);
		}
		String dbDataVersion = readDbDataVersion();
		String appDataVersion = MigrationHelper.getVersion(migrator.getClass());
		if (dbDataVersion != null && !dbDataVersion.equals(appDataVersion)) {
			logger.error("Data version mismatch, please clean the database first");
			System.exit(1);
		}

		Metadata metadata = buildMetadata();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();

		File dataDir = Bootstrap.getCommandDir();
		FileUtils.cleanDir(dataDir);
		BootstrapUtils.unzip(backupFile, dataDir, null);
		importData(metadata, dataDir);

		sessionFactory.close();
		logger.info("Database is restored successfully");
		
		System.exit(0);
	}

}
