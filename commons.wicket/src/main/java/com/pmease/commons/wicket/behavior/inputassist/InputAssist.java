package com.pmease.commons.wicket.behavior.inputassist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InputAssist implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<ErrorMark> errorMarks;
	
	private final List<AssistItem> assistItems;
	
	public InputAssist(List<ErrorMark> errorMarks, List<AssistItem> assistItems) {
		this.errorMarks = join(errorMarks);
		this.assistItems = assistItems;
	}
	
	public List<ErrorMark> getErrorMarks() {
		return errorMarks;
	}

	public List<AssistItem> getAssistItems() {
		return assistItems;
	}
	
	private List<ErrorMark> join(List<ErrorMark> errorMarks) {
		Collections.sort(errorMarks, new Comparator<ErrorMark>() {

			@Override
			public int compare(ErrorMark o1, ErrorMark o2) {
				return o1.getFrom() - o2.getFrom();
			}
			
		});
		List<ErrorMark> joinedErrorMarks = new ArrayList<>();
		ErrorMark currentMark = null;
		for (ErrorMark mark: errorMarks) {
			if (currentMark == null) {
				currentMark = mark;
			} else if (mark.getFrom() <= currentMark.getTo()) {
				currentMark = new ErrorMark(currentMark.getFrom(), Math.max(currentMark.getTo(), mark.getTo()));
			} else {
				joinedErrorMarks.add(currentMark);
				currentMark = mark;
			}
		}
		if (currentMark != null)
			joinedErrorMarks.add(currentMark);
		return joinedErrorMarks;
	}
}
