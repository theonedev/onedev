package com.pmease.commons.jetty;

import org.eclipse.jetty.server.SessionManager;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.AppLoader;

public class JettyModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		bind(SessionManager.class).toProvider(SessionManagerProvider.class);
		if (AppLoader.COMMAND_RESTORE.equals(Bootstrap.getCommand()) 
				|| AppLoader.COMMAND_UPGRADE.equals(Bootstrap.getCommand())
				|| AppLoader.COMMAND_REAPPLY_DB_CONSTRAINTS.equals(Bootstrap.getCommand())) {
			bind(JettyManager.class).to(NoJettyManager.class);
		} else {
			bind(JettyManager.class).to(DefaultJettyManager.class);
		}
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return JettyPlugin.class;
	}

}
