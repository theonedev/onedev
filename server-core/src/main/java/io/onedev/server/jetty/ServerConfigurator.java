package io.onedev.server.jetty;

import org.eclipse.jetty.server.Server;

import io.onedev.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
