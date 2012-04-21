package com.pmease.commons.bootstrap;

import java.io.File;

public interface FileVisitor {
	/**
	 * @param file
	 * @return 
	 * 		false to continue to visit, true to stop visiting 
	 */
	boolean visit(File file);
}
