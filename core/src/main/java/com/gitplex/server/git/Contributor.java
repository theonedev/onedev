package com.gitplex.server.git;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.util.Day;

public class Contributor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final PersonIdent user;
	
	private final Map<Day, Contribution> dailyContributions;
	
	public Contributor(PersonIdent user, Map<Day, Contribution> dailyContributions) {
		this.user = user;
		this.dailyContributions = dailyContributions;
	}

	public PersonIdent getUser() {
		return user;
	}

	public Map<Day, Contribution> getDailyContributions() {
		return dailyContributions;
	}

}
