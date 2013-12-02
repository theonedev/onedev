package com.pmease.commons.util;

import java.io.File;
import java.io.InputStream;

import javax.annotation.Nullable;

import com.pmease.commons.bootstrap.BootstrapUtils;
import com.pmease.commons.bootstrap.StringMatcher;

public class ZipUtils {
	
	/**
	 * Unzip files matching specified matcher from specified file to specified directory.
	 * 
	 * @param srcFile 
	 * 		zip file to extract from
	 * @param 
	 * 		destDir destination directory to extract to
	 * @param matcher 
	 * 		if not null, only entries with name matching this param in the zip will be extracted.
	 * 		Use null if you want to extract all entries from the zip file.  
	 */
	public static void unzip(File srcFile, File destDir, @Nullable StringMatcher matcher) {
		BootstrapUtils.unzip(srcFile, destDir, matcher);
	} 	
	
	/**
	 * Unzip files matching specified matcher from input stream to specified directory.
	 * 
	 * @param is
	 * 			input stream to unzip files from. This method will not close the stream 
	 * 			after using it. Caller should be responsible for closing
	 * @param destDir
	 * 			destination directory to extract files to
	 * @param matcher
	 * 			matcher to select matched files for extracting
	 */
	public static void unzip(InputStream is, File destDir, @Nullable StringMatcher matcher) {
		BootstrapUtils.unzip(is, destDir, matcher);
	} 	

}
