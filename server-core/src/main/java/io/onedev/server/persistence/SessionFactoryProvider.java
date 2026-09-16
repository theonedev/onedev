package io.onedev.server.persistence;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.hibernate.SessionFactory;

@Singleton
public class SessionFactoryProvider implements Provider<SessionFactory> {

	@Inject
	private SessionFactoryService sessionFactoryService;

	@Override
	public SessionFactory get() {
		return sessionFactoryService.getSessionFactory();
	}

}
