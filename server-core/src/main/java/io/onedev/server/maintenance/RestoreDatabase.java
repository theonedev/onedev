package io.onedev.server.maintenance;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.bootstrap.Command;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class RestoreDatabase extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(RestoreDatabase.class);
	
	public static final String COMMAND = "restore-db";
	
	@Inject
	public RestoreDatabase(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, 
			TransactionManager transactionManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator, transactionManager);
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
		
		if (!backupFile.exists()) {
			logger.error("Unable to find file: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
		
		logger.info("Restoring database from {}...", backupFile.getAbsolutePath());
		
		if (Bootstrap.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before restoring");
			System.exit(1);
		}

		Metadata metadata = buildMetadata();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();

		if (backupFile.isFile()) {
			File dataDir = FileUtils.createTempDir("restore");
			try {
				ZipUtils.unzip(backupFile, dataDir);
				doRestore(metadata, dataDir);
			} finally {
				FileUtils.deleteDir(dataDir);
			}
		} else {
			doRestore(metadata, backupFile);
		}

		sessionFactory.close();
		
		if (getDialect().toLowerCase().contains("hsql")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		logger.info("Database is successfully restored from {}", backupFile.getAbsolutePath());
		
		System.exit(0);
	}

	private void doRestore(Metadata metadata, File dataDir) {
		migrateData(dataDir);
		validateData(metadata, dataDir);

		String dbDataVersion = checkDataVersion(true);

		if (dbDataVersion != null) {
			logger.info("Cleaning database...");
			cleanDatabase(metadata);
		}
		
		logger.info("Creating tables...");
		createTables(metadata);
		
		logger.info("Importing data into database...");
		importData(metadata, dataDir);
		
		logger.info("Applying foreign key constraints...");
		try {
			applyConstraints(metadata);		
		} catch (Exception e) {
			logger.error("Failed to apply database constraints", e);
			logger.info("If above error is caused by foreign key constraint violations, you may fix it via your database sql tool, "
					+ "and then run {} to reapply database constraints", Command.getScript("apply-db-constraints"));
			System.exit(1);
		}
	}
	
}
