package com.pmease.commons.hibernate.command;

import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;

import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;

import com.pmease.commons.hibernate.DefaultPersistManager;
import com.pmease.commons.hibernate.IdManager;
import com.pmease.commons.hibernate.ModelProvider;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.migration.Migrator;

@Singleton
public class CheckDataVersionCommand extends DefaultPersistManager {

	@Inject
	public CheckDataVersionCommand(Set<ModelProvider> modelProviders, PhysicalNamingStrategy physicalNamingStrategy,
			@Named("hibernate") Properties properties, Migrator migrator, Interceptor interceptor, 
			IdManager idManager, Dao dao, Validator validator) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, idManager, dao, validator);
	}

	@Override
	public void start() {
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
