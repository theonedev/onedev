package com.pmease.commons.util.trimmable;

import java.util.Iterator;
import java.util.List;

public class TrimUtils {
	
	@SuppressWarnings("unchecked")
	public static void trim(List<?> list, Object context) {
		int index = 0;
		for (Iterator<?> it = list.iterator(); it.hasNext();) {
			Object entry = it.next();
			if (entry instanceof Trimmable) {
				Trimmable trimmable = (Trimmable) entry;
				Object trimmed = trimmable.trim(context);
				if (trimmed == null) {
					it.remove();
				} else {
					if (trimmed != trimmable)
						((List<Object>)list).set(index, trimmed);
					index ++;
				}
			} else {
				index ++;
			}
		}
	}
	
	public static Object trim(AndOrConstruct andOr, Object context) {
		List<?> members = andOr.getMembers();
		
		trim(members, context);
		
		if (members.size() == 1)
			return members.iterator().next();
		else if (members.size() == 0)
			return null;
		else
			return andOr.getSelf();
	}
		
}
