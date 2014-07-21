package com.pmease.gitplex.web.util;

import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class ParamUtils {
	
	public static String getPathFromParams(PageParameters params) {
		List<String> list = Lists.newArrayList();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			list.add(Preconditions.checkNotNull(params.get(i).toString()));
		}
		
		return UrlUtils.concatSegments(list);
	}
	
	public static void addPathToParams(String path, PageParameters params) {
		int i = 0;
		for (String each : Splitter.on("/").omitEmptyStrings().split(path)) {
			params.set(i, each);
			i++;
		}
	}
	
}
