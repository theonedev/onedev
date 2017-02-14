package com.gitplex.commons.hibernate.command;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

import com.gitplex.commons.hibernate.DefaultPersistManager;
import com.gitplex.commons.hibernate.EntityValidator;
import com.gitplex.commons.hibernate.HibernateProperties;
import com.gitplex.commons.hibernate.IdManager;
import com.gitplex.commons.hibernate.ModelProvider;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.migration.Migrator;

@Singleton
public class DefaultDBDialectCommand extends DefaultPersistManager {

	@Inject
	public DefaultDBDialectCommand(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			HibernateProperties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao, EntityValidator validator) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao, validator);
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
