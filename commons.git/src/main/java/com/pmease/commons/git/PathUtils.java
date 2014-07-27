package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * Utils to operate git object path.
 * 
 * @author robin
 *
 */
public class PathUtils {

	public static List<String> split(@Nullable String path) {
		List<String> pathElements = new ArrayList<>();
		if (path != null) {
			for (String element: Splitter.on("/").split(path)) {
				if (element.length() != 0)
					pathElements.add(element);
			}
		}
		return pathElements;
	}
	
	public static @Nullable String join(List<String> pathElements) {
		List<String> nonEmptyElements = new ArrayList<>();
		for (String element: pathElements){
			if (element.length() != 0)
				nonEmptyElements.add(element);
		}
		if (!nonEmptyElements.isEmpty()) {
			return Joiner.on("/").join(nonEmptyElements);
		} else {
			return null;
		}
	}

	public static @Nullable String normalize(@Nullable String path) {
		return join(split(path));
	}
	
}
