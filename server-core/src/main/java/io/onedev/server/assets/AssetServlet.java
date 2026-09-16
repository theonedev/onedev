package io.onedev.server.assets;

import java.io.IOException;
import java.net.URL;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.ee11.servlet.ResourceServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.Resource;

import io.onedev.commons.bootstrap.Bootstrap;

/**
 * Asset servlet to serve static web assets with some default parameters.
 * @author robin
 *
 */
public abstract class AssetServlet extends ResourceServlet {

	private static final long serialVersionUID = 1L;
	
    private org.eclipse.jetty.util.resource.ResourceFactory.Closeable resourceFactory;

    private org.eclipse.jetty.http.content.ValidatingCachingHttpContentFactory contentFactory;

    @Override
    public void init() throws ServletException {
        super.init();
        resourceFactory = org.eclipse.jetty.util.resource.ResourceFactory.closeable();
        var context = ServletContextHandler.getServletContextHandler(getServletContext());
        var pool = new org.eclipse.jetty.io.ByteBufferPool.Sized(context.getServer().getByteBufferPool());
        contentFactory = new org.eclipse.jetty.http.content.ValidatingCachingHttpContentFactory(path -> {
            Resource resource = getResource(org.eclipse.jetty.util.URIUtil.decodePath(path));
            if (resource == null || !resource.exists())
                return null;
            String type = context.getMimeTypes().getMimeByExtension(path);
            if (path.endsWith(".mjs"))
                type = "text/javascript";
            else if (path.equals("/prefetch.json"))
                type = "application/speculationrules+json";
            return new org.eclipse.jetty.http.content.ResourceHttpContent(resource, type, pool);
        }, 1000, pool);
        contentFactory.setMaxCacheSize(Integer.parseInt(getInitParameter("maxCacheSize")));
        contentFactory.setMaxCachedFileSize(Integer.parseInt(getInitParameter("maxCachedFileSize")));
        contentFactory.setMaxCachedFiles(Integer.parseInt(getInitParameter("maxCachedFiles")));
        getResourceService().setHttpContentFactory(contentFactory);
    }

    @Override
    public void destroy() {
        try {
            super.destroy();
        } finally {
            if (contentFactory != null)
                contentFactory.flushCache();
            if (resourceFactory != null)
                resourceFactory.close();
        }
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

			return null;
		}
	}

    @Override
    protected String getEncodedPathInContext(HttpServletRequest request, boolean included) {
        String servletPath = included
                ? (String) request.getAttribute(jakarta.servlet.RequestDispatcher.INCLUDE_SERVLET_PATH)
                : request.getServletPath();
        String pathInfo = included
                ? (String) request.getAttribute(jakarta.servlet.RequestDispatcher.INCLUDE_PATH_INFO)
                : request.getPathInfo();
        if (servletPath == null)
            servletPath = request.getServletPath();
        return org.eclipse.jetty.util.URIUtil.encodePath(servletPath + (pathInfo != null ? pathInfo : ""));
    }

	public final Resource getResource(String pathInContext) {
		ServletContextHandler contextHandler = ServletContextHandler.getServletContextHandler(getServletContext());
		
		for (ServletMapping mapping: contextHandler.getServletHandler().getServletMappings()) {
			if (mapping.getServletName().equals(getServletName())) {
				for (String pathSpec: mapping.getPathSpecs()) {
					String relativePath = null;
					if (pathSpec.startsWith("/~help/")) {
						relativePath = pathInContext.substring("/~help/".length());
					} else if (pathSpec.endsWith("/*")) {
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
						URL url = loadResource(relativePath);
						Resource resource = url != null ? resourceFactory.newResource(url) : null;
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
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cacheControl = getInitParameter("cacheControl");
        if (cacheControl == null)
            cacheControl = !Bootstrap.sandboxMode || Bootstrap.prodMode
                    ? "max-age=86400,public" : "must-revalidate,no-cache,no-store";
        if (request.getDispatcherType() == DispatcherType.ERROR)
            cacheControl = "must-revalidate,no-cache,no-store";
        else if (request.getRequestURI().equals("/favicon.ico"))
            cacheControl = "max-age=86400,public";
        response.setHeader("Cache-Control", cacheControl);
        super.service(request, response);
    }
}
