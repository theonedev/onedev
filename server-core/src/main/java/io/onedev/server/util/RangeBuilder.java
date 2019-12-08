package io.onedev.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeBuilder {

	private final List<List<Long>> ranges = new ArrayList<>();
	
	public RangeBuilder(List<Long> discreteValues, List<Long> allValues) {
		Map<Long, Integer> indexes = new HashMap<>();
		for (int i=0; i<allValues.size(); i++)
			indexes.put(allValues.get(i), i);
		List<Long> continuousValues = new ArrayList<>();
		int lastIndex = -1;
		for (Long value: discreteValues) {
			Integer index = indexes.get(value);
			if (index != null) {
				if (lastIndex != -1 && index - lastIndex > 1) {
					ranges.add(continuousValues);
					continuousValues = new ArrayList<>();
				}
				continuousValues.add(value);
				lastIndex = index;
			}
		}
		if (!continuousValues.isEmpty())
			ranges.add(continuousValues);
	}
	
	public List<List<Long>> getRanges() {
		return ranges;
	}
	
}
