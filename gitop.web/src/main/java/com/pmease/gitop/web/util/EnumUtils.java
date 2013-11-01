package com.pmease.gitop.web.util;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class EnumUtils {

	private EnumUtils() {
	}
	
	public static <T extends Enum<T>> T inc(Enum<T> v) {
		Class<T> clazz = v.getDeclaringClass();
		int ordinal = Math.min(v.ordinal() + 1, clazz.getEnumConstants().length - 1);
		return clazz.getEnumConstants()[ordinal];
	}
	
	public static <T extends Enum<T>> T dec(Enum<T> v) {
		int ordinal = Math.max(v.ordinal() - 1,  0);
		return v.getDeclaringClass().getEnumConstants()[ordinal];
	}
	
	public static <T extends Enum<T>> List<T> toList(Class<T> clazz) {
		return ImmutableList.<T>copyOf(clazz.getEnumConstants());
	}
}
