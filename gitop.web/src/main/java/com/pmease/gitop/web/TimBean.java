package com.pmease.gitop.web;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class TimBean extends ChildBean {
	private int score;

	@Editable
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
}
