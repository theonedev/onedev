package com.pmease.commons.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;

import com.google.inject.matcher.Matchers;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.hibernate.dao.GeneralDaoImpl;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

public class HibernateModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(NamingStrategy.class).to(ImprovedNamingStrategy.class);
		
		bind(PersistService.class).to(PersistServiceImpl.class);
		bind(SessionFactory.class).toProvider(PersistServiceImpl.class);
		bind(Configuration.class).toProvider(ConfigurationProvider.class);
		bind(UnitOfWork.class).to(UnitOfWorkImpl.class);
		bind(Session.class).toProvider(UnitOfWorkImpl.class);
		bind(SessionProvider.class).to(UnitOfWorkImpl.class);
		
		bind(GeneralDao.class).to(GeneralDaoImpl.class);
		
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
