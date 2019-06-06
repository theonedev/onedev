package io.onedev.server.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

@Singleton
public class SessionProvider implements Provider<Session> {

	private final SessionManager sessionManager;
	
	@Inject
	public SessionProvider(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	@Override
	public Session get() {
		return sessionManager.getSession();
	}

}
