package com.gitplex.server.product;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.server.git.GitFilter;
import com.gitplex.server.git.GitPostReceiveCallback;
import com.gitplex.server.git.GitPreReceiveCallback;
import com.gitplex.server.persistence.HibernateFilter;
import com.gitplex.server.security.DefaultWebEnvironment;
import com.gitplex.server.util.jetty.ClasspathAssetServlet;
import com.gitplex.server.util.jetty.FileAssetServlet;
import com.gitplex.server.util.jetty.ServletConfigurator;
import com.gitplex.server.util.serverconfig.ServerConfig;
import com.gitplex.server.web.assets.Assets;
import com.gitplex.server.web.behavior.markdown.AttachmentUploadServlet;
import com.gitplex.server.web.websocket.WebSocketManager;

public class ProductServletConfigurator implements ServletConfigurator {

	private final ServerConfig serverConfig;
	
	private final HibernateFilter hibernateFilter;
	
	private final ShiroFilter shiroFilter;
	
    private final GitFilter gitFilter;
    
	private final GitPreReceiveCallback preReceiveServlet;
	
	private final GitPostReceiveCallback postReceiveServlet;
	
	private final WicketServlet wicketServlet;

	private final WebSocketManager webSocketManager;
	
	@Inject
	public ProductServletConfigurator(ServerConfig serverConfig, HibernateFilter hibernateFilter, 
			ShiroFilter shiroFilter, GitFilter gitFilter, GitPreReceiveCallback preReceiveServlet, 
			GitPostReceiveCallback postReceiveServlet, WicketServlet wicketServlet, 
			WebSocketManager webSocketManager) {
		this.serverConfig = serverConfig;
		this.hibernateFilter = hibernateFilter;
		this.shiroFilter = shiroFilter;
        this.gitFilter = gitFilter;
		this.preReceiveServlet = preReceiveServlet;
		this.postReceiveServlet = postReceiveServlet;
		this.wicketServlet = wicketServlet;
		this.webSocketManager = webSocketManager;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.setContextPath("/");
		
		context.getSessionHandler().getSessionManager().setMaxInactiveInterval(serverConfig.getSessionTimeout());
		
		FilterHolder filterHolder = new FilterHolder(hibernateFilter);
		context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
		
		context.setInitParameter(EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, DefaultWebEnvironment.class.getName());
		
		context.addEventListener(new EnvironmentLoaderListener());

		filterHolder = new FilterHolder(shiroFilter);
		context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
		
        filterHolder = new FilterHolder(gitFilter);
        context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
		
		ServletHolder servletHolder = new ServletHolder(preReceiveServlet);
		context.addServlet(servletHolder, GitPreReceiveCallback.PATH + "/*");
        
		servletHolder = new ServletHolder(postReceiveServlet);
        context.addServlet(servletHolder, GitPostReceiveCallback.PATH + "/*");
        
		servletHolder = new ServletHolder(wicketServlet);
		
		/*
		 * Add wicket servlet as the default servlet which will serve all requests failed to 
		 * match a path pattern
		 */
		context.addServlet(servletHolder, "/");
		context.addServlet(AttachmentUploadServlet.class, "/attachment_upload");
		
		servletHolder = new ServletHolder(new ClasspathAssetServlet(Assets.class));
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
		
		/*
		 * Configure a servlet to serve contents under site folder. Site folder can be used 
		 * to hold site specific web assets.   
		 */
		servletHolder = new ServletHolder(new FileAssetServlet(Bootstrap.getSiteDir()));
		context.addServlet(servletHolder, "/site/*");
		context.addServlet(servletHolder, "/robots.txt");
	}

}
