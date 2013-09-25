package com.pmease.gitop.core;

import javax.inject.Inject;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ServletContextConfigurator;

public class GitServletContextConfigurator implements ServletContextConfigurator {

	private final GitServlet gitServlet;
	
	@Inject
	public GitServletContextConfigurator(GitServlet gitServlet) {
		this.gitServlet = gitServlet;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(gitServlet);
		context.addServlet(servletHolder, "/git/*");
		
		servletHolder = new ServletHolder(new org.eclipse.jgit.http.server.GitServlet());
		servletHolder.setInitParameter("base-path", "w:/temp/git");
		servletHolder.setInitParameter("export-all", "1");
		context.addServlet(servletHolder, "/jgit/*");
	}

}
