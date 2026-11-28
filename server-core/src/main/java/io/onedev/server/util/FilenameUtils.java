package io.onedev.server.util;

import java.util.Locale;

public class FilenameUtils {

	public static boolean isWebSafeImage(String fileName) {
		fileName = fileName.toLowerCase(Locale.ROOT);
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".gif") || fileName.endsWith(".png");
	}

	public static String sanitizeFileName(String fileName) {
		return fileName.replace("..", "_").replace('/', '_').replace('\\', '_');
	}
	
}