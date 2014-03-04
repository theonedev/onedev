package com.pmease.gitop.web.util;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class EnumUtils {

	private EnumUtils() {
	}
	
	public static <T extends Enum<T>> T inc(Enum<T> v) {
		Class<T> clazz = v.getDeclaringClass();
		return inc(v, clazz.getEnumConstants()[clazz.getEnumConstants().length - 1]);
	}
	
	public static <T extends Enum<T>> T inc(Enum<T> v, T max) {
		Class<T> clazz = v.getDeclaringClass();
		if (v.ordinal() + 1 >= clazz.getEnumConstants().length - 1) {
			return max;
		}
		
		int ordinal = (v.ordinal() + 1) % clazz.getEnumConstants().length;
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
