package com.pmease.commons.jetty;

import org.eclipse.jetty.server.Server;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
