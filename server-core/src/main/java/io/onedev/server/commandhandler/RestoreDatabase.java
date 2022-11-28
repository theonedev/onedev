package io.onedev.server.commandhandler;

import java.io.File;
import java.sql.Connection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.bootstrap.Command;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.persistence.ConnectionCallable;
import io.onedev.server.persistence.DataManager;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class RestoreDatabase extends AbstractPlugin {

	private static final Logger logger = LoggerFactory.getLogger(RestoreDatabase.class);
	
	public static final String COMMAND = "restore-db";
	
	private final DataManager dataManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public RestoreDatabase(DataManager dataManager, SessionFactoryManager sessionFactoryManager, 
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
		
		if (!backupFile.exists()) {
			logger.error("Unable to find file: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
		
		boolean validateData = true;
		if (Bootstrap.command.getArgs().length >= 2)
			validateData = Boolean.parseBoolean(Bootstrap.command.getArgs()[1]);
		
		logger.info("Restoring database from {}...", backupFile.getAbsolutePath());
		
		if (OneDev.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before restoring");
			System.exit(1);
		}

		sessionFactoryManager.start();
		
		if (backupFile.isFile()) {
			File dataDir = FileUtils.createTempDir("restore");
			try {
				FileUtils.unzip(backupFile, dataDir);
				doRestore(dataDir, validateData);
			} finally {
				FileUtils.deleteDir(dataDir);
			}
		} else {
			doRestore(backupFile, validateData);
		}

		if (hibernateConfig.isHSQLDialect()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		logger.info("Database is successfully restored from {}", backupFile.getAbsolutePath());
		
		System.exit(0);
	}

	private void doRestore(File dataDir, boolean validateData) {
		dataManager.migrateData(dataDir);
		
		if (validateData)
			dataManager.validateData(dataDir);

		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				String dbDataVersion = dataManager.checkDataVersion(conn, true);

				if (dbDataVersion != null) {
					logger.info("Cleaning database...");
					dataManager.cleanDatabase(conn);
				}
				
				logger.info("Creating tables...");
				dataManager.createTables(conn);
				
				return null;
			}
			
		});
				
		logger.info("Importing data into database...");
		dataManager.importData(dataDir);

		dataManager.callWithConnection(new ConnectionCallable<Void>() {

			@Override
			public Void call(Connection conn) {
				logger.info("Applying foreign key constraints...");
				try {
					dataManager.applyConstraints(conn);		
				} catch (Exception e) {
					logger.error("Failed to apply database constraints", e);
					logger.info("If above error is caused by foreign key constraint violations, you may fix it via your database sql tool, "
							+ "and then run {} to reapply database constraints", Command.getScript("apply-db-constraints"));
					System.exit(1);
				}
				return null;
			}
			
		});
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}
	
}
