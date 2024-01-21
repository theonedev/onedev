package io.onedev.server.util;

import java.util.*;
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

	public static List<Map<String, ?>> mapAsList(Map<?, ?> map, 
			String keyName, String valueName) {
		List<Map<String, ?>> list = new ArrayList<>();
		for (Map.Entry<?, ?> entry: map.entrySet()) {
			Map<String, Object> entryMap = new LinkedHashMap<>();
			entryMap.put(keyName, entry.getKey());
			entryMap.put(valueName, entry.getValue());
			list.add(entryMap);
		}
		return list;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> sorted = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) 
            sorted.put(entry.getKey(), entry.getValue());
		
        return sorted;
	}
	
	public static <T> void move(List<T> list, int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			for (int i=0; i<toIndex-fromIndex; i++)
				Collections.swap(list, fromIndex+i, fromIndex+i+1);
		} else {
			for (int i=0; i<fromIndex-toIndex; i++)
				Collections.swap(list, fromIndex-i, fromIndex-i-1);
		}
	}
}
