package com.gitplex.commons.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.gitplex.calla.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
