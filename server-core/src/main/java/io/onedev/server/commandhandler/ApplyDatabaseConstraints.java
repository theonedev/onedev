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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class ApplyDatabaseConstraints extends CommandHandler {

	public static final String COMMAND = "apply-db-constraints";
	
	private static final Logger logger = LoggerFactory.getLogger(ApplyDatabaseConstraints.class);
	
	private final SessionFactoryService sessionFactoryService;
	
	private final DataService dataService;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public ApplyDatabaseConstraints(SessionFactoryService sessionFactoryService, DataService dataService,
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
			if (doMaintenance(() -> {
				sessionFactoryService.start();
				try (var conn = dataService.openConnection()) {
					return callWithTransaction(conn, () -> {
						dataService.checkDataVersion(conn, false);

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

						dataService.dropConstraints(conn);
						dataService.applyConstraints(conn);
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
		sessionFactoryService.stop();
	}

}
