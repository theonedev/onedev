package io.onedev.server.git;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.util.Day;

public class Contributor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final PersonIdent author;
	
	private final Contribution totalContribution;
	
	private final Map<Day, Integer> dailyContributions;
	
	public Contributor(PersonIdent author, Contribution totalContribution, Map<Day, Integer> dailyContributions) {
		this.author = author;
		this.totalContribution = totalContribution;
		this.dailyContributions = dailyContributions;
	}

	public PersonIdent getAuthor() {
		return author;
	}

	public Contribution getTotalContribution() {
		return totalContribution;
	}

	public Map<Day, Integer> getDailyContributions() {
		return dailyContributions;
	}

}
