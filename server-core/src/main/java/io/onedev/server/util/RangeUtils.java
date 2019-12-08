package io.onedev.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.onedev.commons.utils.LinearRange;

public class RangeUtils {
	
	public static List<LinearRange> merge(Collection<LinearRange> ranges) {
		List<LinearRange> sorted = new ArrayList<>(ranges);
		sorted.sort((o1, o2) -> o1.getFrom() - o2.getFrom());
		List<LinearRange> merged = new ArrayList<>();
		LinearRange current = null;
		for (LinearRange range: sorted) {
			if (current == null) {
				current = range;
			} else if (range.getFrom() <= current.getTo()) {
				current = new LinearRange(current.getFrom(), Math.max(current.getTo(), range.getTo()));
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
