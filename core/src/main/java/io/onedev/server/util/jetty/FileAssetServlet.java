package io.onedev.server.util.jetty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet serves static web assets from file system.
 * 
 * @author robin
 *
 */
public class FileAssetServlet extends AssetServlet {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(FileAssetServlet.class);
	
	private final File assetDir;
	
	/**
	 * Construct a file asset servlet using specified directory.
	 * 
	 * @param assetDir 
	 * 			Specify the directory to search asset inside. Asset will be searched recursively.  
	 */
	public FileAssetServlet(File assetDir) {
		this.assetDir = assetDir;
	}

	@Override
	protected URL loadResource(String relativePath) {
		if (logger.isTraceEnabled()) {
			logger.trace("Loading file resource '{}' from directory '{}'...", 
					relativePath, assetDir.getAbsolutePath());
		}
		
		File assetFile = new File(assetDir, relativePath);
		if (assetFile.exists()) {
			try {
				return assetFile.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

}
