package com.gitplex.core;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Interceptor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.core.entity.Account;
import com.gitplex.core.manager.AccountManager;
import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.hibernate.DefaultPersistManager;
import com.gitplex.commons.hibernate.EntityValidator;
import com.gitplex.commons.hibernate.HibernateProperties;
import com.gitplex.commons.hibernate.IdManager;
import com.gitplex.commons.hibernate.ModelProvider;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.migration.Migrator;

@Singleton
public class ResetAdminPasswordCommand extends DefaultPersistManager {

	private static final Logger logger = LoggerFactory.getLogger(ResetAdminPasswordCommand.class);
	
	private final AccountManager accountManager;
	
	@Inject
	public ResetAdminPasswordCommand(Set<ModelProvider> modelProviders, 
			PhysicalNamingStrategy physicalNamingStrategy, HibernateProperties properties, 
			Migrator migrator, Interceptor interceptor, IdManager idManager, Dao dao, 
			EntityValidator validator, AccountManager accountManager) {
		super(modelProviders, physicalNamingStrategy, properties, migrator, interceptor, 
				idManager, dao, validator);
		this.accountManager = accountManager;
	}

	@Override
	public void start() {
		if (Bootstrap.command.getArgs().length == 0) {
			logger.error("Missing password parameter. Usage: {} <new password>", Bootstrap.command.getScript());
			System.exit(1);
		}
		if (Bootstrap.getServerRunningFile().exists()) {
			logger.error("Please stop server before resetting admin password");
			System.exit(1);
		}

		checkDataVersion(false);

		Metadata metadata = buildMetadata();
		sessionFactory = metadata.getSessionFactoryBuilder().applyInterceptor(interceptor).build();
		
		Account root = accountManager.get(Account.ADMINISTRATOR_ID);
		if (root == null) {
			logger.error("Server not set up yet");
			System.exit(1);
		}
		String password = Bootstrap.command.getArgs()[0];
		root.setPassword(password);
		accountManager.save(root);
		
		// wait for a short period to have embedded db flushing data
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		sessionFactory.close();
		
		logger.info("Password of user '" + root.getName() + "' has been reset to: " + password);
		System.exit(0);
	}

}
