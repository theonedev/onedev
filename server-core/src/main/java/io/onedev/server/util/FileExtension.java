package io.onedev.server.util;

import javax.annotation.Nullable;

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
		// No Extension
		return "";
	}
}
