package io.onedev.server.commandhandler;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.data.DataService;
import io.onedev.server.persistence.SessionFactoryService;
import io.onedev.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;

@Singleton
public class CleanDatabase extends CommandHandler {

	public static final String COMMAND = "clean-db";
	
	private static final Logger logger = LoggerFactory.getLogger(CleanDatabase.class);
	
	private final SessionFactoryService sessionFactoryService;
	
	private final DataService dataService;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public CleanDatabase(SessionFactoryService sessionFactoryService, DataService dataService,
                         HibernateConfig hibernateConfig) {
		super(hibernateConfig);
		this.sessionFactoryService = sessionFactoryService;
		this.dataService = dataService;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

		try {
			doMaintenance(() -> {
				sessionFactoryService.start();

				// Run this in autocommit mode as some sqls in the clean script may fail
				// when drop non-existent constraints, and we want to ignore them and 
				// continue to execute other sql statements without rolling back whole 
				// transaction
				try (var conn = dataService.openConnection()) {
					conn.setAutoCommit(true);
					dataService.checkDataVersion(conn, false);
					dataService.cleanDatabase(conn);
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}

				if (hibernateConfig.isHSQLDialect()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				logger.info("Database is cleaned successfully");

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
