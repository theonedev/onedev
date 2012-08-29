package com.pmease.commons.util;

import java.util.ArrayList;
import java.util.List;

public class EasyList {
	
	public static <T> List<T> of(T...objects) {
		List<T> list = new ArrayList<T>();
		for (int i=0; i<objects.length; i++) 
			list.add(objects[i]);
		return list;
	}
	
}
