package io.onedev.server.commandhandler;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.data.DataManager;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class ApplyDatabaseConstraints extends CommandHandler {

	public static final String COMMAND = "apply-db-constraints";
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyDatabaseConstraints.class);
	
	private final SessionFactoryManager sessionFactoryManager;
	
	private final DataManager dataManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public ApplyDatabaseConstraints(SessionFactoryManager sessionFactoryManager, DataManager dataManager, 
									HibernateConfig hibernateConfig) {
		super(hibernateConfig);
		this.sessionFactoryManager = sessionFactoryManager;
		this.dataManager = dataManager;
		this.hibernateConfig = hibernateConfig;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();
		
		try {
			if (doMaintenance(() -> {
				sessionFactoryManager.start();
				try (var conn = dataManager.openConnection()) {
					return callWithTransaction(conn, () -> {
						dataManager.checkDataVersion(conn, false);

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
								return false;
						}

						dataManager.dropConstraints(conn);
						dataManager.applyConstraints(conn);
						return true;
					});
				}
			})) {
				if (hibernateConfig.isHSQLDialect()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				logger.info("Database constraints is applied successfully");
			}
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
