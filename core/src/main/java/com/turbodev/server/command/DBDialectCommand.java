package com.turbodev.server.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

import com.turbodev.server.persistence.DefaultPersistManager;
import com.turbodev.server.persistence.HibernateProperties;
import com.turbodev.server.persistence.IdManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.util.validation.EntityValidator;

@Singleton
public class DBDialectCommand extends DefaultPersistManager {

	@Inject
	public DBDialectCommand(PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator) {
		super(physicalNamingStrategy, properties, interceptor, idManager, dao, validator);
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
