package com.pmease.commons.util;

import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

public class PathUtils {

    /**
     * Determines if specified longer path is relative to the shorter path. 
     *
     * @param shortPath 
     * 			A string representing shorter path, can be a relative path. The path 
     * 			separator does not matter, and will be converted to "/" at comparing time. 
     * 
     * @param longPath 
     * 			A string representing longer path, can be a relative path. The path 
     * 			separator does not matter, and will be converted to "/" at comparing time.
     * @return 
     * 			path of longer relative to shorter, with path separator being converted to "/", 
     * 			and leading character will be "/" if longer is relative to shorter. A null value will be 
     * 			returned if longer is not relative to shorter, or "" if shorter is the same as longer
     */
    public static String parseRelative(String longPath, String shortPath) {
    	shortPath = FilenameUtils.normalizeNoEndSeparator(shortPath);
    	shortPath = StringUtils.replace(shortPath.trim(), "\\", "/");
    	if (shortPath.length() != 0 && shortPath.charAt(0) != '/')
    		shortPath = "/" + shortPath;
    	if (shortPath.endsWith("/"))
    		shortPath = StringUtils.stripEnd(shortPath, "/");

    	longPath = FilenameUtils.normalizeNoEndSeparator(longPath);
    	longPath = StringUtils.replace(longPath.trim(), "\\", "/");
    	if (longPath.length() != 0 && longPath.charAt(0) != '/')
    		longPath = "/" + longPath;
    	if (longPath.endsWith("/"))
    		longPath = StringUtils.stripEnd(longPath, "/");
        
		if (!longPath.startsWith(shortPath))
			return null;
		
		String relativePath = longPath.substring(shortPath.length());
		if (relativePath.length() == 0)
			return relativePath;
		
		if (relativePath.charAt(0) != '/')
			return null;
		else
			return relativePath;
    }

    /**
     * Match specified path against a collection of base paths to find out the longest match.
     * For instance, if collection contains /path1/path2/path3 and /path1/path2, the eager
     * match for /path1/path2/path3/path4 should be /path1/path2/path3. 
     * 
     * @param basePaths
     * 			Collection of base paths to match against specified path.
     * @param path
     * 			The path used for eager matching.
     * @return
     * 			The longest matching base path. Null if no any matched base paths.
     */
    public static String matchLongest(Collection<String> basePaths, String path) {
    	int unmatchedSegments = Integer.MAX_VALUE;
    	String matchedBasePath = null;
    	
    	for (String each: basePaths) {
    		String relative = parseRelative(path, each);
    		if (relative != null) {
    			int currentSegments = StringUtils.splitAndTrim(relative, "/").size();
    			if (unmatchedSegments > currentSegments) {
    				unmatchedSegments = currentSegments;
    				matchedBasePath = each;
    			}
    		}
    	}
    	
    	return matchedBasePath;
    }

}
