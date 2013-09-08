package com.pmease.commons.hibernate;

import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;

import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.pmease.commons.hibernate.dao.DefaultGeneralDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

public class HibernateModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		// Use an optional binding here in case our client does not like to 
		// start persist service provided by this plugin
		bind(Properties.class).annotatedWith(Names.named("hibernate")).toProvider(Providers.<Properties>of(null));
		bind(NamingStrategy.class).to(ImprovedNamingStrategy.class);
		
		bind(PersistService.class).to(PersistServiceImpl.class);
		bind(SessionFactory.class).toProvider(PersistServiceImpl.class);
		bind(Configuration.class).toProvider(ConfigurationProvider.class);
		bind(UnitOfWork.class).to(UnitOfWorkImpl.class);
		bind(Session.class).toProvider(UnitOfWorkImpl.class);
		bind(SessionProvider.class).to(UnitOfWorkImpl.class);
		
		bind(GeneralDao.class).to(DefaultGeneralDao.class);
		
	    TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
	    requestInjection(transactionInterceptor);
	    
	    bindInterceptor(
	    		Matchers.any(), 
	    		Matchers.annotatedWith(Transactional.class), 
	    		transactionInterceptor);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return HibernatePlugin.class;
	}
	
}
