package com.gitplex.server.core;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.gitplex.commons.jetty.ServletConfigurator;
import com.gitplex.server.core.hookcallback.GitPostReceiveCallback;
import com.gitplex.server.core.hookcallback.GitPreReceiveCallback;

@Singleton
public class CoreServletConfigurator implements ServletConfigurator {

	private final GitPreReceiveCallback preReceiveServlet;
	
	private final GitPostReceiveCallback postReceiveServlet;
	
	@Inject
	public CoreServletConfigurator(GitPreReceiveCallback preReceiveServlet, GitPostReceiveCallback postReceiveServlet) {
		this.preReceiveServlet = preReceiveServlet;
		this.postReceiveServlet = postReceiveServlet;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(preReceiveServlet);
		context.addServlet(servletHolder, GitPreReceiveCallback.PATH + "/*");
        
		servletHolder = new ServletHolder(postReceiveServlet);
        context.addServlet(servletHolder, GitPostReceiveCallback.PATH + "/*");
	}

}
