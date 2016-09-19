package com.pmease.commons.jetty;

import javax.inject.Singleton;

import org.eclipse.jetty.servlet.ServletContextHandler;

@Singleton
public class NoJettyManager implements JettyManager {

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public ServletContextHandler getContextHandler() {
		throw new UnsupportedOperationException();
	}

}
