package com.turbodev.server.util.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.turbodev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
