package io.onedev.server.util;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Splitter;

import io.onedev.commons.utils.PathUtils;

public class PathComparator implements Comparator<String> {

	@Override
	public int compare(String path1, String path2) {
		List<String> segments1 = Splitter.on('/').splitToList(path1);
		List<String> segments2 = Splitter.on('/').splitToList(path2);
		return PathUtils.compare(segments1, segments2);
	}
	
}
