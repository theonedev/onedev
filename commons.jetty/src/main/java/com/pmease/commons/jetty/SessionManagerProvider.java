package com.pmease.commons.jetty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.jetty.server.SessionManager;

@Singleton
public class SessionManagerProvider implements Provider<SessionManager> {

	private final JettyPlugin jettyPlugin;
	
	@Inject
	public SessionManagerProvider(JettyPlugin jettyPlugin) {
		this.jettyPlugin = jettyPlugin;
	}
	
	@Override
	public SessionManager get() {
		return jettyPlugin.getContextHandler().getSessionHandler().getSessionManager();
	}

}
