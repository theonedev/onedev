package com.gitplex.server.util.jetty;

import org.eclipse.jetty.server.Server;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
