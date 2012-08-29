package com.pmease.commons.util;

import java.util.HashSet;
import java.util.Set;

public class EasySet {
	
	public static <T> Set<T> of(T...objects) {
		Set<T> set = new HashSet<T>();
		for (int i=0; i<objects.length; i++) 
			set.add(objects[i]);
		return set;
	}
	
}
