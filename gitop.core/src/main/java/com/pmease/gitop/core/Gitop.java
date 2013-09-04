package com.pmease.gitop.core;

import javax.inject.Singleton;

import com.pmease.commons.loader.AppLoader;

@Singleton
public class Gitop {
	
	public static Gitop getInstance() {
		return AppLoader.getInstance(Gitop.class);
	}
	
	public static <T> T getInstance(Class<T> type) {
		return AppLoader.getInstance(type);
	}
	
}
