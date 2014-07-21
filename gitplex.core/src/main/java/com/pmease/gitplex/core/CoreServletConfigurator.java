package com.pmease.gitplex.core;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.gitplex.core.hookcallback.GitPostReceiveCallback;
import com.pmease.gitplex.core.hookcallback.GitUpdateCallback;

@Singleton
public class CoreServletConfigurator implements ServletConfigurator {

	private final GitUpdateCallback preReceiveServlet;
	
	private final GitPostReceiveCallback postReceiveServlet;
	
	@Inject
	public CoreServletConfigurator(GitUpdateCallback preReceiveServlet, GitPostReceiveCallback postReceiveServlet) {
		this.preReceiveServlet = preReceiveServlet;
		this.postReceiveServlet = postReceiveServlet;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(preReceiveServlet);
		context.addServlet(servletHolder, GitUpdateCallback.PATH + "/*");
        
		servletHolder = new ServletHolder(postReceiveServlet);
        context.addServlet(servletHolder, GitPostReceiveCallback.PATH + "/*");
	}

}
