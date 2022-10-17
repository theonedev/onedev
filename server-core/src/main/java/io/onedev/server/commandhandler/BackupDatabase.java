package io.onedev.server.commandhandler;

import java.io.File;
import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.persistence.ConnectionCallable;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class BackupDatabase extends AbstractPlugin {

	public static final String COMMAND = "backup-db";
	
	private static final Logger logger = LoggerFactory.getLogger(BackupDatabase.class);
	
	private final DataManager dataManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public BackupDatabase(DataManager dataManager, SessionFactoryManager sessionFactoryManager, 
			HibernateConfig hibernateConfig) {
		this.dataManager = dataManager;
		this.sessionFactoryManager = sessionFactoryManager;
		this.hibernateConfig = hibernateConfig;
	}
	
	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing backup file parameter. Usage: {} <path to database backup file>", Bootstrap.command.getScript());
			System.exit(1);
		}
		File backupFile = new File(Bootstrap.command.getArgs()[0]);
		
		if (!backupFile.isAbsolute() && System.getenv("WRAPPER_INIT_DIR") != null)
			backupFile = new File(System.getenv("WRAPPER_INIT_DIR"), backupFile.getPath());
		
		if (backupFile.exists()) {
			logger.error("Backup file already exists: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
		
		if (OneDev.isServerRunning(Bootstrap.installDir) && hibernateConfig.isHSQLDialect()) {
			logger.error("Please stop server before backing up database");
			System.exit(1);
		}

		sessionFactoryManager.start();

		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				dataManager.checkDataVersion(conn, false);
				return null;
			}
			
		});
		
		logger.info("Backing up database to {}...", backupFile.getAbsolutePath());
		
		File tempDir = FileUtils.createTempDir("backup");
		try {
			dataManager.exportData(tempDir);
			FileUtils.zip(tempDir, backupFile, null);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			FileUtils.deleteDir(tempDir);
		}

		logger.info("Database is successfully backed up to {}", backupFile.getAbsolutePath());
		
		System.exit(0);
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
