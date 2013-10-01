package com.pmease.commons.jetty;

import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;

public class JettyModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return JettyPlugin.class;
	}

}
