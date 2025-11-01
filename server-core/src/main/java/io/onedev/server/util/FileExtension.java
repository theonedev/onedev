package io.onedev.server.util;

import org.jspecify.annotations.Nullable;

public class FileExtension {

	@Nullable
	public static String getExtension(@Nullable String filename) {
		if(filename == null) {
			return null;
		}
		
		int lastIndexOfDot = filename.lastIndexOf('.');
		if (lastIndexOfDot != -1) {
			String fileExt = filename.substring(lastIndexOfDot+1);
			return fileExt;
		}
		return "";
	}
	
	@Nullable
	public static String getNameWithoutExtension(@Nullable String filename) {
		if(filename == null) {
			return null;
		}
		
		String extension = getExtension(filename);
		
		if(extension.length() == 0){
			return filename;
		}
		
		return filename.substring(0, filename.length() - extension.length() - 1);
	}
}
