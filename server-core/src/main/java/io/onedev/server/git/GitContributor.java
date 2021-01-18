package io.onedev.server.git;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.util.Day;

public class GitContributor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final PersonIdent author;
	
	private final GitContribution totalContribution;
	
	private final Map<Day, Integer> dailyContributions;
	
	public GitContributor(PersonIdent author, GitContribution totalContribution, Map<Day, Integer> dailyContributions) {
		this.author = author;
		this.totalContribution = totalContribution;
		this.dailyContributions = dailyContributions;
	}

	public PersonIdent getAuthor() {
		return author;
	}

	public GitContribution getTotalContribution() {
		return totalContribution;
	}

	public Map<Day, Integer> getDailyContributions() {
		return dailyContributions;
	}

}
