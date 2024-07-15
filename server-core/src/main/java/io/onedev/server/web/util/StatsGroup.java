package io.onedev.server.web.util;

import io.onedev.server.model.support.TimeGroups;
import io.onedev.server.util.date.Day;
import io.onedev.server.util.date.Month;
import io.onedev.server.util.date.Week;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.persistence.criteria.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum StatsGroup {

	BY_DAY {
		@Override
		public Path<?> getPath(Path<?> embeddedPath) {
			return embeddedPath.get(TimeGroups.PROP_DAY);
		}

		@Override
		public String toString(int groupValue) {
			return new Day(groupValue).toString();
		}

		@Override
		public int getNext(int groupValue) {
			return new Day(new Day(groupValue).getDate().plusDays(1)).getValue();
		}
	}, 
	BY_WEEK {

		@Override
		public Path<?> getPath(Path<?> embeddedPath) {
			return embeddedPath.get(TimeGroups.PROP_WEEK);
		}
		
		@Override
		public String toString(int groupValue) {
			return new Week(groupValue).toString();
		}

		@Override
		public int getNext(int groupValue) {
			return new Week(new Week(groupValue).getDate().plusWeeks(1)).getValue();
		}
		
	}, 
	BY_MONTH {
		
		@Override
		public Path<?> getPath(Path<?> embeddedPath) {
			return embeddedPath.get(TimeGroups.PROP_MONTH);
		}
		
		@Override
		public String toString(int groupValue) {
			return new Month(groupValue).toString();
		}

		@Override
		public int getNext(int groupValue) {
			return new Month(new Month(groupValue).getDate().plusMonths(1)).getValue();
		}
		
	};

	public abstract Path<?> getPath(Path<?> embeddedPath);

	public abstract String toString(int groupValue);
	
	public abstract int getNext(int groupValue);
	
	public <T> List<Pair<String, T>> normalizeData(Map<Integer, T> data, @Nullable T emptyValue) {
		List<Pair<String, T>> normalizedData = new ArrayList<>();
		if (!data.isEmpty()) {
			int minKey = Collections.min(data.keySet());
			int maxKey = Collections.max(data.keySet());
			T lastValue = data.get(minKey);
			int currentKey = minKey;
			while (currentKey <= maxKey) {
				var currentValue = data.get(currentKey);
				if (currentValue == null)
					currentValue = emptyValue != null? emptyValue: lastValue;
				else
					lastValue = currentValue;
				normalizedData.add(new ImmutablePair<>(toString(currentKey), currentValue));
				currentKey = getNext(currentKey);
			}
		}
		return normalizedData;
	}
	
}
