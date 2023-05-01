package io.onedev.server.commandhandler;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.PersistenceManager;
import io.onedev.server.persistence.SessionFactoryManager;
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
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final PersistenceManager persistenceManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public CleanDatabase(SessionFactoryManager sessionFactoryManager, PersistenceManager persistenceManager, 
						 HibernateConfig hibernateConfig) {
		super(hibernateConfig);
		this.sessionFactoryManager = sessionFactoryManager;
		this.persistenceManager = persistenceManager;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

		try {
			doMaintenance(() -> {
				sessionFactoryManager.start();

				// Run this in autocommit mode as some sqls in the clean script may fail
				// when drop non-existent constraints, and we want to ignore them and 
				// continue to execute other sql statements without rolling back whole 
				// transaction
				try (var conn = persistenceManager.openConnection()) {
					conn.setAutoCommit(true);
					persistenceManager.checkDataVersion(conn, false);
					persistenceManager.cleanDatabase(conn);
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
		sessionFactoryManager.stop();
	}

}
