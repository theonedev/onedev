package io.onedev.server.maintenance;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class CheckDataVersion extends DefaultPersistManager {

	public static final String COMMAND = "check-data-version";
	
	private static final Logger logger = LoggerFactory.getLogger(CheckDataVersion.class);
	
	@Inject
	public CheckDataVersion(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, 
			TransactionManager transactionManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator, transactionManager);
	}

	@Override
	public void start() {
		if (Bootstrap.isServerRunning(Bootstrap.installDir) && getDialect().toLowerCase().contains("hsql")) {
			logger.error("Please stop server before checking data version");
			System.exit(1);
		}
		
		// Use system.out in case logger is suppressed by user as this output is important to 
		// upgrade procedure
		System.out.println("Data version: " + checkDataVersion(false));
		System.exit(0);
	}

	@Override
	public SessionFactory getSessionFactory() {
		throw new UnsupportedOperationException();
	}

}
