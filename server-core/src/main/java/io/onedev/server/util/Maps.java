package io.onedev.server.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public class Maps {

	public static <T> Map<T, T> newLinkedHashMap(@SuppressWarnings("unchecked") T...args) {
		Map<T, T> map = new LinkedHashMap<>();
		initMap(map, args);
		return map;
	}
	
	public static <T> Map<T, T> newHashMap(@SuppressWarnings("unchecked") T...args) {
		Map<T, T> map = new HashMap<>();
		initMap(map, args);
		return map;
	}
	
	private static <T> void initMap(Map<T, T> map, @SuppressWarnings("unchecked") T...args) {
		Preconditions.checkArgument(args.length % 2 == 0, "Arguments should be key/value pairs");
		for (int i=0; i<args.length/2; i++)
			map.put(args[i*2], args[i*2+1]);
	}
	
}
