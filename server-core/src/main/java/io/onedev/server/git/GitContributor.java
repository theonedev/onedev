package io.onedev.server.git;

import org.eclipse.jgit.lib.PersonIdent;

import java.io.Serializable;
import java.util.Map;

public class GitContributor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final PersonIdent author;
	
	private final int totalCommits;
	
	private final Map<Integer, Integer> dailyContributions;
	
	public GitContributor(PersonIdent author, int totalCommits, Map<Integer, Integer> dailyContributions) {
		this.author = author;
		this.totalCommits = totalCommits;
		this.dailyContributions = dailyContributions;
	}

	public PersonIdent getAuthor() {
		return author;
	}

	public int getTotalCommits() {
		return totalCommits;
	}

	public Map<Integer, Integer> getDailyContributions() {
		return dailyContributions;
	}

}
