package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPlugin;
import io.onedev.server.OneDev;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.PersistenceManager;
import io.onedev.server.persistence.PersistenceUtils;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class ApplyDatabaseConstraints extends AbstractPlugin {

	public static final String COMMAND = "apply-db-constraints";
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyDatabaseConstraints.class);
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final PersistenceManager persistenceManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public ApplyDatabaseConstraints(SessionFactoryManager sessionFactoryManager, PersistenceManager persistenceManager, 
			HibernateConfig hibernateConfig) {
		this.sessionFactoryManager = sessionFactoryManager;
		this.persistenceManager = persistenceManager;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		if (OneDev.isServerRunning(Bootstrap.installDir)) {
			logger.error("Please stop server before applying db constraints");
			System.exit(1);
		}

		sessionFactoryManager.start();
		
		try (var conn = persistenceManager.openConnection()) {
			callWithTransaction(conn, () -> {
				persistenceManager.checkDataVersion(conn, false);

				logger.warn("This script is mainly used to apply database constraints after fixing database integrity issues (may happen during restore/upgrade)");

				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				while (true) {
					logger.warn("Press 'y' to run the script, or 'n' to stop");
					String input;
					try {
						input = reader.readLine();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					if (input.equalsIgnoreCase("y"))
						break;
					else if (input.equalsIgnoreCase("n"))
						System.exit(0);
				}

				persistenceManager.dropConstraints(conn);
				persistenceManager.applyConstraints(conn);
				return null;
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		if (hibernateConfig.isHSQLDialect()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		logger.info("Database constraints is applied successfully");
		
		System.exit(0);
	}

	@Override
	public void stop() {
		sessionFactoryManager.stop();
	}

}
