package com.pmease.commons.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DefaultPersistService implements PersistService, Provider<SessionFactory> {

	private final Provider<Configuration> configurationProvider;
	
	private volatile SessionFactory sessionFactory;
	
	@Inject
	public DefaultPersistService(Provider<Configuration> configurationProvider) {
		this.configurationProvider = configurationProvider;
	}
	
	@Override
	public void start() {
		Preconditions.checkState(sessionFactory == null);
		if (configurationProvider.get() != null) {
			sessionFactory = configurationProvider.get().buildSessionFactory();
		}
	}

	@Override
	public void stop() {
		if (sessionFactory != null) {
			Preconditions.checkState(!sessionFactory.isClosed());
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	@Override
	public SessionFactory get() {
		Preconditions.checkNotNull(sessionFactory, "Persist service is either not started or is not configured.");
		return sessionFactory;
	}

}
