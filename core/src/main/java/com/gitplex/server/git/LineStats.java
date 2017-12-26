package com.gitplex.server.git;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.gitplex.server.util.Day;

public class LineStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<Day, Map<String, Integer>> dailyLines;
	
	private transient List<String> sortedLanguages;
	
	public LineStats(Map<Day, Map<String, Integer>> dailyLines) {
		this.dailyLines = dailyLines;
	}

	public Map<Day, Map<String, Integer>> getDailyLines() {
		return dailyLines;
	}
	
	@Nullable
	public Day getFirstDay() {
		if (!dailyLines.isEmpty())
			return Collections.min(dailyLines.keySet());
		else
			return null;
	}
	
	@Nullable 
	public Day getLastDay() {
		if (!dailyLines.isEmpty())
			return Collections.max(dailyLines.keySet());
		else
			return null;
	}
	
	public List<String> getSortedLanguages() {
		if (sortedLanguages == null) {
			Map<String, Integer> languageLines = new HashMap<>();
			for (Map<String, Integer> each: dailyLines.values()) {
				for (Map.Entry<String, Integer> entry: each.entrySet()) {
					Integer lines = languageLines.get(entry.getKey());
					if (lines != null)
						lines += entry.getValue();
					else
						lines = entry.getValue();
					languageLines.put(entry.getKey(), lines);
				}
			}
			sortedLanguages = new ArrayList<>(languageLines.keySet());
			Collections.sort(sortedLanguages, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return languageLines.get(o2) - languageLines.get(o1);
				}
				
			});
		}
		return sortedLanguages;
	}
	
}
