package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.data.DataService;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.SQLException;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class BackupDatabase extends CommandHandler {

	public static final String COMMAND = "backup-db";
	
	private static final Logger logger = LoggerFactory.getLogger(BackupDatabase.class);
	
	private final DataService dataService;
	
	private final SessionFactoryService sessionFactoryService;
		
	private File backupFile;
	
	@Inject
	public BackupDatabase(DataService dataService, SessionFactoryService sessionFactoryService,
                          HibernateConfig hibernateConfig) {
		super(hibernateConfig);
		this.dataService = dataService;
		this.sessionFactoryService = sessionFactoryService;
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
		
		if (backupFile.exists()) {
			logger.error("Backup file already exists: {}", backupFile.getAbsolutePath());
			System.exit(1);
		}

		try {
			doMaintenance(() -> {
				sessionFactoryService.start();

				try (var conn = dataService.openConnection()) {
					callWithTransaction(conn, () -> {
						dataService.checkDataVersion(conn, false);
						return null;
					});
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}

				logger.info("Backing up database to {}...", backupFile.getAbsolutePath());

				File tempDir = FileUtils.createTempDir("backup");
				try {
					dataService.exportData(tempDir);
					ZipUtils.zip(tempDir, backupFile, null);
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				} finally {
					FileUtils.deleteDir(tempDir);
				}

				logger.info("Database is successfully backed up to {}", backupFile.getAbsolutePath());
				return null;
			});
			System.exit(0);
		} catch (ExplicitException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	@Override
	public void stop() {
		sessionFactoryService.stop();
	}

}
