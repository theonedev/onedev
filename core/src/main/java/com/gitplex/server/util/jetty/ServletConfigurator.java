package com.gitplex.server.util.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
