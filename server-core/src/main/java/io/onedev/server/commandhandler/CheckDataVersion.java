package io.onedev.server.commandhandler;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.persistence.PersistenceManager;
import io.onedev.server.persistence.SessionFactoryManager;
import io.onedev.server.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.SQLException;

import static io.onedev.server.persistence.PersistenceUtils.callWithTransaction;

@Singleton
public class CheckDataVersion extends CommandHandler {

	public static final String COMMAND = "check-data-version";
	
	private static final Logger logger = LoggerFactory.getLogger(CheckDataVersion.class);

	private final SessionFactoryManager sessionFactoryManager;
	
	private final PersistenceManager persistenceManager;
	
	private final HibernateConfig hibernateConfig;
	
	@Inject
	public CheckDataVersion(SessionFactoryManager sessionFactoryManager, PersistenceManager persistenceManager, 
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

				// Use system.out in case logger is suppressed by user as this output is important to 
				// upgrade procedure
				String dataVersion;
				try (var conn = persistenceManager.openConnection()) {
					dataVersion = callWithTransaction(conn, () -> persistenceManager.checkDataVersion(conn, false));
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				System.out.println("Data version: " + dataVersion);
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
