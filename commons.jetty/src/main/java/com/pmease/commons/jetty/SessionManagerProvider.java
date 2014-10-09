package com.pmease.commons.jetty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.jetty.server.SessionManager;

@Singleton
public class SessionManagerProvider implements Provider<SessionManager> {

	private final JettyPlugin plugin;
	
	@Inject
	public SessionManagerProvider(JettyPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public SessionManager get() {
		return plugin.getContextHandler().getSessionHandler().getSessionManager();
	}

}
