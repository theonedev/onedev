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
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class BackupDatabase extends DefaultPersistManager {

	public static final String COMMAND = "backup-db";
	
	private static final Logger logger = LoggerFactory.getLogger(BackupDatabase.class);
	
	@Inject
	public BackupDatabase(PhysicalNamingStrategy physicalNamingStrategy,
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
		
		if (backupFile.exists()) {
			logger.error("Backup file already exists: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
		
		if (Bootstrap.isServerRunning(Bootstrap.installDir) && getDialect().toLowerCase().contains("hsql")) {
			logger.error("Please stop server before backing up database");
			System.exit(1);
		}
		
		checkDataVersion(false);
		
		logger.info("Backing up database to {}...", backupFile.getAbsolutePath());
		
		Metadata metadata = buildMetadata();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();

		File tempDir = FileUtils.createTempDir("backup");
		try {
			exportData(tempDir);
			ZipUtils.zip(tempDir, backupFile);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			FileUtils.deleteDir(tempDir);
		}

		sessionFactory.close();
		logger.info("Database is successfully backed up to {}", backupFile.getAbsolutePath());
		
		System.exit(0);
	}

}
