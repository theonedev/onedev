package com.pmease.commons.jetty.extensionpoints;

import org.eclipse.jetty.server.Server;

public interface ServerConfigurator {
	void configure(Server server);
}
