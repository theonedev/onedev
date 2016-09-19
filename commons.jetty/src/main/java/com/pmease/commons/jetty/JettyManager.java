package com.pmease.commons.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

public interface JettyManager {

	void start();
	
	void stop();
	
	ServletContextHandler getContextHandler();
	
}
