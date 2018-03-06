package io.onedev.server.util.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import io.onedev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
