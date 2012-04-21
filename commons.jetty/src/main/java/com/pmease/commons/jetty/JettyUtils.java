package com.pmease.commons.jetty;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.bootstrap.Bootstrap;

public class JettyUtils {
	
	/**
	 * Create Jetty resource servlet.
	 * @param servletContext
	 */
	public static ServletHolder createResourceServletHolder() {
        ServletHolder servletHolder = new ServletHolder(new DefaultServlet());

        servletHolder.setInitParameter("maxCacheSize", "256000000");
        servletHolder.setInitParameter("maxCachedFileSize", "200000000");
        servletHolder.setInitParameter("maxCachedFiles", "2048");
        servletHolder.setInitParameter("resourceCache", "resourceCache");
        
        if (!Bootstrap.isSandboxMode() || Bootstrap.isProdMode()) {
	        servletHolder.setInitParameter("dirAllowed", "false");
	        servletHolder.setInitParameter("cacheControl", "max-age=3600,public");
        }
		
		return servletHolder;
	}
}
