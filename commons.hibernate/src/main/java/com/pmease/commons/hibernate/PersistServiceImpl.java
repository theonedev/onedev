package com.pmease.commons.hibernate;

import org.hibernate.SessionFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PersistServiceImpl implements PersistService, Provider<SessionFactory> {

	private final ConfigurationProvider configurationProvider;
	
	private volatile SessionFactory sessionFactory;
	
	@Inject
	public PersistServiceImpl(ConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}
	
	@SuppressWarnings("deprecation")
	public void start() {
		Preconditions.checkState(sessionFactory == null);
		if (configurationProvider.get() != null)
			sessionFactory = configurationProvider.get().buildSessionFactory();
	}

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
