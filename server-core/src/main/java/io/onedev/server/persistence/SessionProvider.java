package io.onedev.server.persistence;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

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
