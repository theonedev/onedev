package io.onedev.server.jetty;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
