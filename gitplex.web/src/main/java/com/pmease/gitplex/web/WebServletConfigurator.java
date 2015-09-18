package com.pmease.gitplex.web;

import javax.inject.Singleton;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.gitplex.web.assets.Assets;
import com.pmease.gitplex.web.page.test.UploadServlet;

@Singleton
public class WebServletConfigurator implements ServletConfigurator {

	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(new ClasspathAssetServlet(Assets.class));
		context.addServlet(servletHolder, "/assets/*");
		context.addServlet(servletHolder, "/favicon.ico");
		
		context.addServlet(UploadServlet.class, "/upload");
	}

}
