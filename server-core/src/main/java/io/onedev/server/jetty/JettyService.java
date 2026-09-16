package io.onedev.server.jetty;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;

public interface JettyService {
	
	void start();
	
	void stop();
	
	ServletContextHandler getServletContextHandler();
	
}
