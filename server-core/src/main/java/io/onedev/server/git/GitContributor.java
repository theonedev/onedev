package io.onedev.server.git;

import org.eclipse.jgit.lib.PersonIdent;

import java.io.Serializable;
import java.util.Map;

public class GitContributor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final PersonIdent author;
	
	private final GitContribution totalContribution;
	
	private final Map<Integer, Integer> dailyContributions;
	
	public GitContributor(PersonIdent author, GitContribution totalContribution, Map<Integer, Integer> dailyContributions) {
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

	public Map<Integer, Integer> getDailyContributions() {
		return dailyContributions;
	}

}
