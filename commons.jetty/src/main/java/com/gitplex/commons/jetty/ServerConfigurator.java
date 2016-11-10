package com.gitplex.commons.jetty;

import org.eclipse.jetty.server.Server;

import com.gitplex.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
