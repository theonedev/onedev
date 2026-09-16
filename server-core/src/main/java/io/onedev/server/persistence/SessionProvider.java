package io.onedev.server.persistence;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.hibernate.Session;

@Singleton
public class SessionProvider implements Provider<Session> {

	private final SessionService sessionService;
	
	@Inject
	public SessionProvider(SessionService sessionService) {
		this.sessionService = sessionService;
	}
	
	@Override
	public Session get() {
		return sessionService.getSession();
	}

}
