package com.gitplex.commons.util;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.calla.loader.LoaderUtils;
import com.gitplex.jsymbol.Range;

public class PathUtils {

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
    		String relative = LoaderUtils.parseRelative(path, each);
    		if (relative != null) {
    			int currentSegments = LoaderUtils.splitAndTrim(relative, "/").size();
    			if (unmatchedSegments > currentSegments) {
    				unmatchedSegments = currentSegments;
    				matchedBasePath = each;
    			}
    		}
    	}
    	
    	return matchedBasePath;
    }

    public @Nullable static Range matchSegments(String path, String match, boolean ignoreExt) {
    	int beginIndex = path.indexOf(match);
    	while (beginIndex != -1) {
    		int endIndex = beginIndex+match.length();
    		char leftCh = beginIndex==0?'/':path.charAt(beginIndex-1);
    		char rightCh = endIndex==path.length()?'/':path.charAt(endIndex);
    		if (leftCh == '/' && (rightCh=='/' || ignoreExt&&rightCh=='.'))
    			return new Range(beginIndex, endIndex);
    		beginIndex = path.indexOf(match, beginIndex+1);
    	}
    	return null;
    }
    
}
