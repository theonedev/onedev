package com.gitplex.server.git;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jgit.lib.PersonIdent;

public class UserContribution implements Serializable {

	private static final long serialVersionUID = 1L;

	private final PersonIdent user;
	
	private final List<DayAndCommits> dayAndCommits;
	
	public UserContribution(PersonIdent user, List<DayAndCommits> dayAndCommits) {
		this.user = user;
		this.dayAndCommits = dayAndCommits;
	}

	public PersonIdent getUser() {
		return user;
	}

	public List<DayAndCommits> getDayAndCommits() {
		return dayAndCommits;
	}

}
