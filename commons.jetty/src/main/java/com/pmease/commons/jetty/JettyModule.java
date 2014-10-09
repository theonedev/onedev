package com.pmease.commons.jetty;

import org.eclipse.jetty.server.SessionManager;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

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
