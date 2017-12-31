package com.gitplex.server.git;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.gitplex.server.util.Day;

public class LineStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<Day, Map<String, Integer>> linesByDay;
	
	private transient Map<String, Map<Day, Integer>> linesByLanguage;
	
	public LineStats(Map<Day, Map<String, Integer>> linesByDay) {
		this.linesByDay = linesByDay;
	}

	/**
	 * Get source lines by day and language
	 * @return
	 */
	public Map<Day, Map<String, Integer>> getLinesByDay() {
		return linesByDay;
	}
	
	@Nullable
	public Day getFirstDay() {
		if (!linesByDay.isEmpty())
			return Collections.min(linesByDay.keySet());
		else
			return null;
	}
	
	@Nullable 
	public Day getLastDay() {
		if (!linesByDay.isEmpty())
			return Collections.max(linesByDay.keySet());
		else
			return null;
	}
	
	/**
	 * Get lines by language and day
	 * @return
	 */
	public Map<String, Map<Day, Integer>> getLinesByLanguage() {
		if (linesByLanguage == null) {
			linesByLanguage = new HashMap<>();
			for (Map.Entry<Day, Map<String, Integer>> outerEntry: linesByDay.entrySet()) {
				Day day = outerEntry.getKey();
				for (Map.Entry<String, Integer> innerEntry: outerEntry.getValue().entrySet()) {
					String language = innerEntry.getKey();
					int lines = innerEntry.getValue();
					Map<Day, Integer> linesOfLanguage = linesByLanguage.get(language);
					if (linesOfLanguage == null) {
						linesOfLanguage = new HashMap<>();
						linesByLanguage.put(language, linesOfLanguage);
					}
					linesOfLanguage.put(day, lines);
				}
			}
		}
		return linesByLanguage;
	}
	
}
