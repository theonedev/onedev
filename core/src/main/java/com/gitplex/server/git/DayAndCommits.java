package com.gitplex.server.git;

import java.io.Serializable;

import com.gitplex.server.util.Day;

public class DayAndCommits implements Serializable, Comparable<DayAndCommits> {

	private static final long serialVersionUID = 1L;

	private final Day day;
	
	private final int commits;

	public DayAndCommits(Day day, int commits) {
		this.day = day;
		this.commits = commits;
	}
	
	public Day getDay() {
		return day;
	}

	public int getCommits() {
		return commits;
	}

	@Override
	public int compareTo(DayAndCommits o) {
		return day.compareTo(o.day);
	}
	
}