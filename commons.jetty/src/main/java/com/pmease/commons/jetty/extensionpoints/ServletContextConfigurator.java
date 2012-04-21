package com.pmease.commons.jetty.extensionpoints;

import org.eclipse.jetty.servlet.ServletContextHandler;

public interface ServletContextConfigurator {
	void configure(ServletContextHandler context);
}
