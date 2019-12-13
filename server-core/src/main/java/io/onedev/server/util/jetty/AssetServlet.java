package io.onedev.server.util.jetty;

import java.io.IOException;
import java.net.URL;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.ResourceService;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.Resource;

import io.onedev.commons.launcher.bootstrap.Bootstrap;

/**
 * Asset servlet to serve static web assets with some default parameters.
 * @author robin
 *
 */
public abstract class AssetServlet extends DefaultServlet {

	private static final long serialVersionUID = 1L;
	
	private static ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<HttpServletRequest>();

	public AssetServlet() {
		super(new ResourceService() {
			
			@Override
			protected void putHeaders(HttpServletResponse response, HttpContent content, long contentLength) {
				super.putHeaders(response, content, contentLength);
				
				HttpFields fields;
				if (response instanceof Response)
					fields = ((Response) response).getHttpFields();
				else
					fields = ((Response)((HttpServletResponseWrapper) response).getResponse()).getHttpFields();
				
				if (requestHolder.get().getDispatcherType() == DispatcherType.ERROR) {
					/*
					 * Do not cache error page and also makes sure that error page is not eligible for 
					 * modification check. That is, error page will be always retrieved.
					 */
		            fields.put(HttpHeader.CACHE_CONTROL, "must-revalidate,no-cache,no-store");
				} else if (requestHolder.get().getRequestURI().equals("/favicon.ico")) {
					/*
					 * Make sure favicon request is cached. Otherwise, it will be requested for every 
					 * page request.
					 */
					fields.put(HttpHeader.CACHE_CONTROL, "max-age=86400,public");
				}
			}
			
		});
	}
	@Override
	public String getInitParameter(String name) {
		String value = super.getInitParameter(name);
		if (value != null) {
			return value;
		} else {
			if (name.equals("maxCacheSize"))
				return "256000000";
			if (name.equals("maxCachedFileSize"))
				return "200000000";
			if (name.equals("maxCachedFiles"))
				return "2048";
			if (name.equals("gzip"))
				return "false";
			if (name.equals("dirAllowed"))
			    return "false";
			if (name.equals("cacheControl")) {
		        if (!Bootstrap.sandboxMode || Bootstrap.prodMode)
			        return "max-age=31536000,public";
		        else
		        	return "must-revalidate,no-cache,no-store";
			}
			return null;
		}
	}

	@Override
	public final Resource getResource(String pathInContext) {
		ServletContextHandler.Context context = (ServletContextHandler.Context) getServletContext();
		ServletContextHandler contextHandler = (ServletContextHandler) context.getContextHandler();
		
		for (ServletMapping mapping: contextHandler.getServletHandler().getServletMappings()) {
			if (mapping.getServletName().equals(getServletName())) {
				for (String pathSpec: mapping.getPathSpecs()) {
					String relativePath = null;
					if (pathSpec.endsWith("/*")) {
						pathSpec = StringUtils.substringBeforeLast(pathSpec, "/*");
						if (pathInContext.startsWith(pathSpec + "/")) 
							relativePath = pathInContext.substring(pathSpec.length());
					} else if (pathSpec.startsWith("*.")) {
						pathSpec = StringUtils.stripStart(pathSpec, "*");
						if (pathInContext.endsWith(pathSpec))
							relativePath = pathInContext;
					} else if (pathSpec.equals(pathInContext)) {
						relativePath = pathInContext;
					}
					if (relativePath != null) {
						relativePath = StringUtils.stripStart(relativePath, "/");
						Resource resource = Resource.newResource(loadResource(relativePath));
						if (resource != null && resource.exists())
							return resource;
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Load resource based on specified relative path. 
	 * 
	 * @param relativePath 
	 * 			Relative path is the URL used to access asset relative to path spec the servlet mounting to.
	 * 			For instance, if the servlet is mounted to /asset/*, and if the URL used to access asset is 
	 * 			/asset/images/test.gif, the relative path will then be images/test.gif. 
	 * @return
	 * 			The URI used to access content of the asset specified by relative path. Null if the asset 
	 * 			does not exist.
	 */
	protected abstract URL loadResource(String relativePath);
	
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			requestHolder.set((HttpServletRequest) request);
			super.doGet(request, response);
		} finally {
			requestHolder.set(null);
		}
	}

}
