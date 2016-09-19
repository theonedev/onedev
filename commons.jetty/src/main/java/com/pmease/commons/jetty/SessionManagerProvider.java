package com.pmease.commons.jetty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.jetty.server.SessionManager;

@Singleton
public class SessionManagerProvider implements Provider<SessionManager> {

	private final JettyManager jettyManager;
	
	@Inject
	public SessionManagerProvider(JettyManager jettyManager) {
		this.jettyManager = jettyManager;
	}
	
	@Override
	public SessionManager get() {
		return jettyManager.getContextHandler().getSessionHandler().getSessionManager();
	}

}
