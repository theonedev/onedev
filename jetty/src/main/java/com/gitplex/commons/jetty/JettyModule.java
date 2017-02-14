package com.gitplex.commons.jetty;

import org.eclipse.jetty.server.SessionManager;

import com.gitplex.calla.loader.AbstractPlugin;
import com.gitplex.calla.loader.AbstractPluginModule;

public class JettyModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(SessionManager.class).toProvider(SessionManagerProvider.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return JettyPlugin.class;
	}

}
