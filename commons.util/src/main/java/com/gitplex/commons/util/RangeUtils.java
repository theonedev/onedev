package com.gitplex.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RangeUtils {
	
	public static List<Range> merge(Collection<Range> ranges) {
		List<Range> sorted = new ArrayList<>(ranges);
		sorted.sort((o1, o2) -> o1.getFrom() - o2.getFrom());
		List<Range> merged = new ArrayList<>();
		Range current = null;
		for (Range range: sorted) {
			if (current == null) {
				current = range;
			} else if (range.getFrom() <= current.getTo()) {
				current = new Range(current.getFrom(), Math.max(current.getTo(), range.getTo()));
			} else {
				merged.add(current);
				current = range;
			}
		}
		if (current != null)
			merged.add(current);
		return merged;
	}
	
}
