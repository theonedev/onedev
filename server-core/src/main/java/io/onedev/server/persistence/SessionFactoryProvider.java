package io.onedev.server.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

@Singleton
public class SessionFactoryProvider implements Provider<SessionFactory> {

	private final SessionFactoryManager sessionFactoryManager;
	
	@Inject
	public SessionFactoryProvider(SessionFactoryManager sessionFactoryManager) {
		this.sessionFactoryManager = sessionFactoryManager;
	}
	
	@Override
	public SessionFactory get() {
		return sessionFactoryManager.getSessionFactory();
	}

}
