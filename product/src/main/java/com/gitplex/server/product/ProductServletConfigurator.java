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
import org.glassfish.jersey.servlet.ServletContainer;

import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.server.git.GitFilter;
import com.gitplex.server.git.GitPostReceiveCallback;
import com.gitplex.server.git.GitPreReceiveCallback;
import com.gitplex.server.security.GitPlexWebEnvironment;
import com.gitplex.server.util.jetty.ClasspathAssetServlet;
import com.gitplex.server.util.jetty.FileAssetServlet;
import com.gitplex.server.util.jetty.ServletConfigurator;
import com.gitplex.server.util.serverconfig.ServerConfig;
import com.gitplex.server.web.assets.Assets;
import com.gitplex.server.web.component.markdown.AttachmentUploadServlet;
import com.gitplex.server.web.websocket.WebSocketManager;

public class ProductServletConfigurator implements ServletConfigurator {

	private final ServerConfig serverConfig;
	
	private final ShiroFilter shiroFilter;
	
    private final GitFilter gitFilter;
    
	private final GitPreReceiveCallback preReceiveServlet;
	
	private final GitPostReceiveCallback postReceiveServlet;
	
	private final WicketServlet wicketServlet;
	
	private final ServletContainer jerseyServlet;

	private final WebSocketManager webSocketManager;
	
	@Inject
	public ProductServletConfigurator(ServerConfig serverConfig, ShiroFilter shiroFilter, GitFilter gitFilter, 
			GitPreReceiveCallback preReceiveServlet, GitPostReceiveCallback postReceiveServlet, 
			WicketServlet wicketServlet, WebSocketManager webSocketManager, ServletContainer jerseyServlet) {
		this.serverConfig = serverConfig;
		this.shiroFilter = shiroFilter;
        this.gitFilter = gitFilter;
		this.preReceiveServlet = preReceiveServlet;
		this.postReceiveServlet = postReceiveServlet;
		this.wicketServlet = wicketServlet;
		this.webSocketManager = webSocketManager;
		this.jerseyServlet = jerseyServlet;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.setContextPath("/");
		
		context.getSessionHandler().getSessionManager().setMaxInactiveInterval(serverConfig.getSessionTimeout());
		
		context.setInitParameter(EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, GitPlexWebEnvironment.class.getName());
		context.addEventListener(new EnvironmentLoaderListener());
		FilterHolder shiroFilterHolder = new FilterHolder(shiroFilter);
		context.addFilter(shiroFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));
		
        FilterHolder gitFilterHolder = new FilterHolder(gitFilter);
        context.addFilter(gitFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));
		
		ServletHolder preReceiveServletHolder = new ServletHolder(preReceiveServlet);
		context.addServlet(preReceiveServletHolder, GitPreReceiveCallback.PATH + "/*");
        
		ServletHolder postReceiveServletHolder = new ServletHolder(postReceiveServlet);
        context.addServlet(postReceiveServletHolder, GitPostReceiveCallback.PATH + "/*");
        
		ServletHolder wicketServletHolder = new ServletHolder(wicketServlet);
		
		/*
		 * Add wicket servlet as the default servlet which will serve all requests failed to 
		 * match a path pattern
		 */
		context.addServlet(wicketServletHolder, "/");
		
		context.addServlet(AttachmentUploadServlet.class, "/attachment_upload");
		
		ServletHolder assetServletHolder = new ServletHolder(new ClasspathAssetServlet(Assets.class));
		context.addServlet(assetServletHolder, "/assets/*");
		context.addServlet(assetServletHolder, "/favicon.ico");
		
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
		ServletHolder fileServletHolder = new ServletHolder(new FileAssetServlet(Bootstrap.getSiteDir()));
		context.addServlet(fileServletHolder, "/site/*");
		context.addServlet(fileServletHolder, "/robots.txt");
		
		ServletHolder jerseyServletHolder = new ServletHolder(jerseyServlet);
		context.addServlet(jerseyServletHolder, "/rest/*");
		context.addServlet(jerseyServletHolder, "/api/v3/*"); // GitHub api compatible
	}

}
