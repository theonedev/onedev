package com.gitplex.server.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public class EasyMap {
	
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> fill(Map<K, V> map, Object...objects) {
		Preconditions.checkArgument(objects.length % 2 == 0, "Number of arguments should be even.");
		for (int i=0; i<objects.length / 2; i++) 
			map.put((K)objects[i*2], (V)objects[i*2+1]);

		return map;
	}
	
	public static Map<Object, Object> of(Object...objects) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		fill(map, objects);
		return map;
	}
	
	public static Map<String, String> of(String...strings) {
		Map<String, String> map = new HashMap<String, String>();
		fill(map, (Object[])strings);
		return map;
	}
	
	public static Map<Object, Object> ofOrdered(Object...objects) {
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		fill(map, objects);
		return map;
	}

	public static Map<String, String> ofOrdered(String...strings) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		fill(map, (Object[])strings);
		return map;
	}

}
