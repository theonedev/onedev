package com.turbodev.server.util.jetty;

import org.eclipse.jetty.server.Server;

import com.turbodev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
