package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.bootstrap.Command;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.data.DataManager;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.SQLException;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class RestoreDatabase extends CommandHandler {

	private static final Logger logger = LoggerFactory.getLogger(RestoreDatabase.class);
	
	public static final String COMMAND = "restore-db";
	
	private final DataManager dataManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final HibernateConfig hibernateConfig;
	
	private File backupFile;
	
	@Inject
	public RestoreDatabase(DataManager dataManager, SessionFactoryManager sessionFactoryManager,
                           HibernateConfig hibernateConfig) {
		super(hibernateConfig);
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
		
		backupFile = new File(Bootstrap.command.getArgs()[0]);
		if (!backupFile.isAbsolute() && System.getenv("WRAPPER_INIT_DIR") != null)
			backupFile = new File(System.getenv("WRAPPER_INIT_DIR"), backupFile.getPath());
		
		if (!backupFile.exists()) {
			logger.error("Unable to find file: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}
		
		logger.info("Restoring database from {}...", backupFile.getAbsolutePath());

		try {
			doMaintenance(() -> {
				sessionFactoryManager.start();

				if (backupFile.isFile()) {
					File dataDir = FileUtils.createTempDir("restore");
					try {
						FileUtils.unzip(backupFile, dataDir);
						doRestore(dataDir);
					} finally {
						FileUtils.deleteDir(dataDir);
					}
				} else {
					doRestore(backupFile);
				}

				if (hibernateConfig.isHSQLDialect()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				logger.info("Database is successfully restored from {}", backupFile.getAbsolutePath());
				return null;
			});
			System.exit(0);
		} catch (ExplicitException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	private void doRestore(File dataDir) {
		dataManager.migrateData(dataDir);
		
		try (var conn = dataManager.openConnection()) {
			callWithTransaction(conn, () -> {
				String dbDataVersion = dataManager.checkDataVersion(conn, true);

				if (dbDataVersion != null) {
					logger.info("Cleaning database...");
					dataManager.cleanDatabase(conn);
				}

				logger.info("Creating tables...");
				dataManager.createTables(conn);

				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
				
		logger.info("Importing data into database...");
		dataManager.importData(dataDir);

		try (var conn = dataManager.openConnection()) {
			callWithTransaction(conn, () -> {
				logger.info("Applying foreign key constraints...");
				try {
					dataManager.applyConstraints(conn);
				} catch (Exception e) {
					var message = String.format("Failed to apply database constraints. If this error is caused by " +
							"foreign key constraint violations, you may fix it via your database sql tool, and " +
							"then run %s to reapply database constraints", 
							Command.getScript("apply-db-constraints"));
					throw new RuntimeException(message);
				}
				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}
	
}
