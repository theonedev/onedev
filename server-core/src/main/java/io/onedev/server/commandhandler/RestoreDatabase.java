package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.bootstrap.Command;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.PersistenceManager;
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
	
	private final PersistenceManager persistenceManager;
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final HibernateConfig hibernateConfig;
	
	private File backupFile;
	
	@Inject
	public RestoreDatabase(PersistenceManager persistenceManager, SessionFactoryManager sessionFactoryManager, 
						   HibernateConfig hibernateConfig) {
		super(hibernateConfig);
		this.persistenceManager = persistenceManager;
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
		
		boolean validateData;
		if (Bootstrap.command.getArgs().length >= 2)
			validateData = Boolean.parseBoolean(Bootstrap.command.getArgs()[1]);
		else 
			validateData = true;
		
		logger.info("Restoring database from {}...", backupFile.getAbsolutePath());

		try {
			doMaintenance(() -> {
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
				return null;
			});
			System.exit(0);
		} catch (ExplicitException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	private void doRestore(File dataDir, boolean validateData) {
		persistenceManager.migrateData(dataDir);
		
		if (validateData)
			persistenceManager.validateData(dataDir);

		try (var conn = persistenceManager.openConnection()) {
			callWithTransaction(conn, () -> {
				String dbDataVersion = persistenceManager.checkDataVersion(conn, true);

				if (dbDataVersion != null) {
					logger.info("Cleaning database...");
					persistenceManager.cleanDatabase(conn);
				}

				logger.info("Creating tables...");
				persistenceManager.createTables(conn);

				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
				
		logger.info("Importing data into database...");
		persistenceManager.importData(dataDir);

		try (var conn = persistenceManager.openConnection()) {
			callWithTransaction(conn, () -> {
				logger.info("Applying foreign key constraints...");
				try {
					persistenceManager.applyConstraints(conn);
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
