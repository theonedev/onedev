package io.onedev.server.product;

import java.io.File;
import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import io.onedev.server.OneDev;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.apache.wicket.protocol.http.WicketServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.server.agent.ServerSocketServlet;
import io.onedev.server.git.GitFilter;
import io.onedev.server.git.GitLfsFilter;
import io.onedev.server.git.GoGetFilter;
import io.onedev.server.git.hook.GitPostReceiveCallback;
import io.onedev.server.git.hook.GitPreReceiveCallback;
import io.onedev.server.jetty.ClasspathAssetServlet;
import io.onedev.server.jetty.FileAssetServlet;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.security.DefaultWebEnvironment;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.img.ImageScope;
import io.onedev.server.web.websocket.WebSocketManager;

public class ProductServletConfigurator implements ServletConfigurator {

	private static final int SESSION_TIMEOUT = 300;
	
	private final ShiroFilter shiroFilter;
	
    private final GitFilter gitFilter;
    
    private final GoGetFilter goGetFilter;
    
    private final GitLfsFilter gitLfsFilter;
    
	private final GitPreReceiveCallback preReceiveServlet;
	
	private final GitPostReceiveCallback postReceiveServlet;
	
	private final WicketServlet wicketServlet;
	
	private final ServletContainer jerseyServlet;
	
	private final WebSocketManager webSocketManager;
	
	private final ServerSocketServlet serverServlet;
	
	@Inject
	public ProductServletConfigurator(ShiroFilter shiroFilter, 
			GitFilter gitFilter, GitLfsFilter gitLfsFilter, GitPreReceiveCallback preReceiveServlet, 
			GitPostReceiveCallback postReceiveServlet, WicketServlet wicketServlet, 
			WebSocketManager webSocketManager, ServletContainer jerseyServlet, 
			ServerSocketServlet serverServlet, GoGetFilter goGetFilter) {
		this.shiroFilter = shiroFilter;
        this.gitFilter = gitFilter;
        this.gitLfsFilter = gitLfsFilter;
		this.preReceiveServlet = preReceiveServlet;
		this.postReceiveServlet = postReceiveServlet;
		this.wicketServlet = wicketServlet;
		this.webSocketManager = webSocketManager;
		this.jerseyServlet = jerseyServlet;
		this.serverServlet = serverServlet;
		this.goGetFilter = goGetFilter;
	}
	
	@Override
	public void configure(ServletContextHandler context) {
		context.setContextPath("/");
		
		context.getSessionHandler().setMaxInactiveInterval(SESSION_TIMEOUT);
		
		context.setInitParameter(EnvironmentLoader.ENVIRONMENT_CLASS_PARAM, DefaultWebEnvironment.class.getName());
		context.addEventListener(new EnvironmentLoaderListener());
		context.addFilter(new FilterHolder(shiroFilter), "/*", EnumSet.allOf(DispatcherType.class));
		
        context.addFilter(new FilterHolder(gitFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(gitLfsFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(goGetFilter), "/*", EnumSet.allOf(DispatcherType.class));
		
		context.addServlet(new ServletHolder(preReceiveServlet), GitPreReceiveCallback.PATH + "/*");
        
        context.addServlet(new ServletHolder(postReceiveServlet), GitPostReceiveCallback.PATH + "/*");
        
		/*
		 * Add wicket servlet as the default servlet which will serve all requests failed to 
		 * match a path pattern
		 */
		context.addServlet(new ServletHolder(wicketServlet), "/");
		
		context.addServlet(new ServletHolder(new ClasspathAssetServlet(ImageScope.class)), "/~img/*");
		context.addServlet(new ServletHolder(new ClasspathAssetServlet(IconScope.class)), "/~icon/*");
		
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
		File assetsDir = OneDev.getAssetsDir();
		
		boolean hasCustomLogo = false;
		boolean hasSiteMapTxt = false;
		boolean hasSiteMapXml = false;
		for (File file: assetsDir.listFiles()) {
			if (file.isFile()) {
				context.addServlet(new ServletHolder(new FileAssetServlet(assetsDir)), "/" + file.getName());
				if (file.getName().equals("logo.png"))
					hasCustomLogo = true;
				else if (file.getName().equals("sitemap.xml"))
					hasSiteMapXml = true;
				else if (file.getName().equals("sitemap.txt"))
					hasSiteMapTxt = true;
			} else {
				context.addServlet(new ServletHolder(new FileAssetServlet(file)), "/" + file.getName() + "/*");
			}
		}
		if (!hasCustomLogo)
			context.addServlet(new ServletHolder(new FileAssetServlet(assetsDir)), "/logo.png");
		if (!hasSiteMapTxt)
			context.addServlet(new ServletHolder(new FileAssetServlet(assetsDir)), "/sitemap.txt");
		if (!hasSiteMapXml)
			context.addServlet(new ServletHolder(new FileAssetServlet(assetsDir)), "/sitemap.xml");
		
		var incompatibilitiesDir = new File(Bootstrap.installDir, "incompatibilities");
		for (var file: incompatibilitiesDir.listFiles())
			context.addServlet(new ServletHolder(new FileAssetServlet(incompatibilitiesDir)), "/~help/" + file.getName());
		
		context.addServlet(new ServletHolder(jerseyServlet), "/~api/*");	
		context.addServlet(new ServletHolder(serverServlet), "/~server");
	}
	
}
