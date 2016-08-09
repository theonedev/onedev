package com.pmease.gitplex.web;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.ServletConfigurator;
import com.pmease.commons.wicket.websocket.WebSocketManager;
import com.pmease.gitplex.web.assets.Assets;

@Singleton
public class WebServletConfigurator implements ServletConfigurator {

	private final WebSocketManager webSocketManager;
	
	@Inject
	public WebServletConfigurator(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		ServletHolder servletHolder = new ServletHolder(new ClasspathAssetServlet(Assets.class));
		context.addServlet(servletHolder, "/assets/*");
		context.addServlet(servletHolder, "/favicon.ico");
		context.getSessionHandler().addEventListener(new HttpSessionListener() {

			@Override
			public void sessionCreated(HttpSessionEvent se) {
			}

			@Override
			public void sessionDestroyed(HttpSessionEvent se) {
				webSocketManager.onDestroySession(se.getSession().getId());
			}
			
		});
	}
	
}
