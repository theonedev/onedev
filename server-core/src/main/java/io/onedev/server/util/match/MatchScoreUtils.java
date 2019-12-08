package io.onedev.server.util.match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MatchScoreUtils {

	public static <T> List<T> filterAndSort(Collection<T> list, MatchScoreProvider<T> matchScoreProvider) {
		Map<T, Double> matchScores = new HashMap<>();
		List<T> newList = new ArrayList<>();
		for (T item: list) {
			double matchScore = matchScoreProvider.getMatchScore(item);
			if (matchScore > 0) {
				matchScores.put(item, matchScore);
				newList.add(item);
			}
		}
		Collections.sort(newList, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				double result = matchScores.get(o1) - matchScores.get(o2);
				if (result < 0)
					return 1;
				else if (result > 0)
					return -1;
				else
					return 0;
			}
			
		});
		
		return newList;
	}

	public static double getMatchScore(@Nullable String text, @Nullable String query) {
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
