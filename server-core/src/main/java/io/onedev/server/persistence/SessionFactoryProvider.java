package io.onedev.server.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

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
