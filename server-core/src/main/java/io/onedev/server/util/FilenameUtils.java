package io.onedev.server.util;

public class FilenameUtils extends org.apache.commons.io.FilenameUtils {

	public static String sanitizeFilename(String fileName) {
		return fileName.replace("..", "_").replace('/', '_').replace('\\', '_');
	}

}