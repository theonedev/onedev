package com.pmease.commons.wicket;

public class ImageUtils {
	
	public static boolean isWebSafe(String fileName) {
		fileName = fileName.toLowerCase();
		return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") 
				|| fileName.endsWith(".gif") || fileName.endsWith(".png");
	}
	
}
