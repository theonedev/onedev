package com.pmease.commons.util.trimmable;

import java.util.Iterator;
import java.util.List;

public class TrimUtils {
	
	public static void trim(List<Trimmable> list) {
		int index = 0;
		for (Iterator<Trimmable> it = list.iterator(); it.hasNext();) {
			Trimmable trimmable = it.next();
			Trimmable trimmed = trimmable.trim();
			if (trimmed == null) {
				it.remove();
			} else {
				if (trimmed != trimmable)
					list.set(index, trimmed);
				index ++;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Trimmable trim(AndOrConstruct andOr) {
		List<? extends Trimmable> members = andOr.getMembers();
		
		trim((List<Trimmable>) members);
		
		if (members.size() == 1)
			return members.iterator().next();
		else if (members.size() == 0)
			return null;
		else
			return andOr.getSelf();
	}
		
}
