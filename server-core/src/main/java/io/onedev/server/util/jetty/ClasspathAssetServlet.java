package io.onedev.server.util.jetty;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet serves static web assets from classpath. 
 * 
 * @author robin
 *
 */
public class ClasspathAssetServlet extends AssetServlet {

	private static final long serialVersionUID = 1L;
	
	private final Class<?> packageLocator;
	
	private static final Logger logger = LoggerFactory.getLogger(ClasspathAssetServlet.class);
	
	/**
	 * Construct a classpath asset servlet using specified package locator.
	 * 
	 * @param packageLocator 
	 * 			Asset will be loaded from the package containing the locator class. Asset will be 
	 * 			searched in this package and sub packages recursively.  
	 */
	public ClasspathAssetServlet(Class<?> packageLocator) {
		this.packageLocator = packageLocator;
	}

	@Override
	protected URL loadResource(String relativePath) {
		if (logger.isTraceEnabled()) {
			logger.trace("Loading classpath resource '{}' from package '{}'...", 
					relativePath, packageLocator.getPackage().getName());
		}
		return packageLocator.getResource(relativePath);
	}

}
