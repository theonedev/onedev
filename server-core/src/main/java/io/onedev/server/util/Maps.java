package io.onedev.server.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public class Maps {

	public static Map<Object, Object> newLinkedHashMap(Object...args) {
		Map<Object, Object> map = new LinkedHashMap<>();
		initMap(map, args);
		return map;
	}
	
	public static Map<Object, Object> newHashMap(Object...args) {
		Map<Object, Object> map = new HashMap<>();
		initMap(map, args);
		return map;
	}
	
	private static void initMap(Map<Object, Object> map, Object...args) {
		Preconditions.checkArgument(args.length % 2 == 0, "Arguments should be key/value pairs");
		for (int i=0; i<args.length/2; i++)
			map.put(args[i*2], args[i*2+1]);
	}
	
}
