package io.onedev.server.util.jetty;

import org.eclipse.jetty.server.Server;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
