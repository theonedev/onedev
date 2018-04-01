package io.onedev.server.web.page.project.stats;

import java.io.Serializable;

import io.onedev.server.git.Contribution;
import io.onedev.server.util.Day;

class DayContribution implements Serializable, Comparable<DayContribution> {

	private static final long serialVersionUID = 1L;

	private final Day day;
	
	private final Contribution contribution;
	
	public DayContribution(Day day, Contribution contribution) {
		this.day = day;
		this.contribution = contribution;
	}
	
	public Day getDay() {
		return day;
	}

	public Contribution getContribution() {
		return contribution;
	}

	@Override
	public int compareTo(DayContribution o) {
		return day.compareTo(o.day);
	}
	
}