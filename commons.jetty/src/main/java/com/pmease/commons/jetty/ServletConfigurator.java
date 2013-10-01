package com.pmease.commons.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
