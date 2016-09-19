package com.pmease.commons.jetty;

import com.google.inject.Inject;
import com.pmease.commons.loader.AbstractPlugin;

public class JettyPlugin extends AbstractPlugin {
	
	private final JettyManager jettyManager;
	
	@Inject
	public JettyPlugin(JettyManager jettyManager) {
		this.jettyManager = jettyManager;
	}
	
	@Override
	public void start() {
		jettyManager.start();
	}

	@Override
	public void stop() {
		jettyManager.stop();
	}

}
