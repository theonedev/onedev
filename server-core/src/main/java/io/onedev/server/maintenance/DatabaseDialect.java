package io.onedev.server.maintenance;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

import io.onedev.server.persistence.DefaultPersistManager;
import io.onedev.server.persistence.HibernateProperties;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.validation.EntityValidator;

@Singleton
public class DatabaseDialect extends DefaultPersistManager {

	public static final String COMMAND = "db_dialect";

	@Inject
	public DatabaseDialect(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator, 
			TransactionManager transactionManager) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator, transactionManager);
	}

	@Override
	public void start() {
		// Use system.out in case logger is suppressed by user as this output is important to 
		// upgrade procedure
		System.out.println("Database dialect: " + getDialect());
		System.exit(0);
	}

	@Override
	public SessionFactory getSessionFactory() {
		throw new UnsupportedOperationException();
	}

}
