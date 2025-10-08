package io.onedev.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public abstract class Similarities<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	public Similarities(Collection<T> collection) {
		Map<T, Double> similarScores = new HashMap<>();
		List<T> list = new ArrayList<>();
		for (T item: collection) {
			double similarScore = getSimilarScore(item);
			if (similarScore > 0) {
				similarScores.put(item, similarScore);
				list.add(item);
			}
		}
		Collections.sort(list, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				double result = similarScores.get(o1) - similarScores.get(o2);
				if (result < 0)
					return 1;
				else if (result > 0)
					return -1;
				else
					return 0;
			}
			
		});
		addAll(list);
	}
	
	protected abstract double getSimilarScore(T item);
	
	public static double getSimilarScore(@Nullable String text, @Nullable String query) {
		if (query == null)
			query = "";
		else
			query = query.toLowerCase().trim();
		if (text == null)
			text = "";
		else
			text = text.toLowerCase().trim();

		if (query.length() != 0) {
			int index = text.indexOf(query);
			if (index == -1) 
				return -1;
			else
				return query.length() * 1.0 / text.length();
		} else {
			return 1;
		}
	}
	
}
