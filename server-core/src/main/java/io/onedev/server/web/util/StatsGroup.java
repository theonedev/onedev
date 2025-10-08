package io.onedev.server.web.util;

import io.onedev.server.model.support.TimeGroups;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.Path;

import static io.onedev.server.web.translation.Translation._T;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public enum StatsGroup {

	BY_DAY {
		@Override
		public Path<?> getPath(Path<?> embeddedPath) {
			return embeddedPath.get(TimeGroups.PROP_DAY);
		}

		@Override
		public String toString(int groupValue) {
			var localDate = LocalDate.ofEpochDay(groupValue);
			return String.format("%02d-%02d", localDate.getMonthValue(), localDate.getDayOfMonth());
		}

		@Override
		public int getNext(int groupValue) {
			return (int) LocalDate.ofEpochDay(groupValue).plusDays(1).toEpochDay();
		}
		
	}, 
	BY_WEEK {

		@Override
		public Path<?> getPath(Path<?> embeddedPath) {
			return embeddedPath.get(TimeGroups.PROP_WEEK);
		}
		
		@Override
		public String toString(int groupValue) {
			var localDate = LocalDate.ofEpochDay(groupValue);
			var weekFields = WeekFields.of(Locale.getDefault());
			return String.format(_T("w%02d"), localDate.get(weekFields.weekOfWeekBasedYear()));
		}

		@Override
		public int getNext(int groupValue) {
			return (int) LocalDate.ofEpochDay(groupValue).plusWeeks(1).toEpochDay();
		}
		
	}, 
	BY_MONTH {
		
		@Override
		public Path<?> getPath(Path<?> embeddedPath) {
			return embeddedPath.get(TimeGroups.PROP_MONTH);
		}
		
		@Override
		public String toString(int groupValue) {
			var localDate = LocalDate.ofEpochDay(groupValue);
			return String.format("%04d-%02d", localDate.getYear(), localDate.getMonthValue());
		}

		@Override
		public int getNext(int groupValue) {
			return (int) LocalDate.ofEpochDay(groupValue).plusMonths(1).toEpochDay();
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
