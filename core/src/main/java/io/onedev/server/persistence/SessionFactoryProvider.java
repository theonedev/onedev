package io.onedev.server.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

@Singleton
public class SessionFactoryProvider implements Provider<SessionFactory> {

	private final PersistManager persistManager;
	
	@Inject
	public SessionFactoryProvider(PersistManager persistManager) {
		this.persistManager = persistManager;
	}
	
	@Override
	public SessionFactory get() {
		return persistManager.getSessionFactory();
	}

}
