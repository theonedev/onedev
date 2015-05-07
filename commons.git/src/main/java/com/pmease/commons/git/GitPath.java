package com.pmease.commons.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class GitPath implements Serializable, Comparable<GitPath> {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final int mode;
	
	public GitPath(String name, int mode) {
		this.name = name;
		this.mode = mode;
	}

	public String getName() {
		return name;
	}

	public int getMode() {
		return mode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GitPath) 
			return name.equals(((GitPath)obj).getName());
		else 
			return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(GitPath path) {
		if (FileMode.TREE.equals(mode)) {
			if (FileMode.TREE.equals(path.mode)) 
				return compare(name, path.name);
			else
				return -1;
		} else if (FileMode.TREE.equals(path.mode)) {
			return 1;
		} else {
			return compare(name, path.name);
		}
	}

	public static int compare(@Nullable String path1, @Nullable String path2) {
		List<String> segments1 = split(path1);
		List<String> segments2 = split(path2);
	
		int index = 0;
		for (String segment1: segments1) {
			if (index<segments2.size()) {
				int result = segment1.compareTo(segments2.get(index));
				if (result != 0)
					return result;
			} else {
				return 1;
			}
			index++;
		}
		if (index<segments2.size())
			return -1;
		else
			return 0;
	}

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

	public static @Nullable String join(List<String> pathSegments) {
		List<String> nonEmptyElements = new ArrayList<>();
		for (String element: pathSegments){
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
		return GitPath.join(GitPath.split(path));
	}
	
}