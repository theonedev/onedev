package io.onedev.server.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {
	
	public static <T> Set<T> findDuplicates(Collection<T> collection) {
	    Set<T> uniques = new HashSet<>();
	    return collection.stream().filter(e -> !uniques.add(e)).collect(Collectors.toSet());
	}

	public static <T> Map<T, T> newLinkedHashMap(@SuppressWarnings("unchecked") T...args) {
		Map<T, T> map = new LinkedHashMap<>();
		CollectionUtils.initMap(map, args);
		return map;
	}

	public static <T> Map<T, T> newHashMap(@SuppressWarnings("unchecked") T...args) {
		Map<T, T> map = new HashMap<>();
		CollectionUtils.initMap(map, args);
		return map;
	}

	static <T> void initMap(Map<T, T> map, @SuppressWarnings("unchecked") T...args) {
		Preconditions.checkArgument(args.length % 2 == 0, "Arguments should be key/value pairs");
		for (int i=0; i<args.length/2; i++)
			map.put(args[i*2], args[i*2+1]);
	}

}
