package com.pmease.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class JoinedRanges implements Iterable<Range> {
	
	private final List<Range> result;
	
	public JoinedRanges(List<Range> ranges) {
		Collections.sort(ranges, new Comparator<Range>() {

			@Override
			public int compare(Range o1, Range o2) {
				return o1.getFrom() - o2.getFrom();
			}
			
		});
		result = new ArrayList<>();
		Range currentRange = null;
		for (Range range: ranges) {
			if (currentRange == null) {
				currentRange = range;
			} else if (range.getFrom() <= currentRange.getTo()) {
				currentRange = new Range(currentRange.getFrom(), Math.max(currentRange.getTo(), range.getTo()));
			} else {
				result.add(currentRange);
				currentRange = range;
			}
		}
		if (currentRange != null)
			result.add(currentRange);
	}

	@Override
	public Iterator<Range> iterator() {
		return result.iterator();
	}

}
