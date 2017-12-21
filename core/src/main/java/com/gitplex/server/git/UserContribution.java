package com.gitplex.server.git;

import java.io.Serializable;
import java.util.Map;

public class UserContribution implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String email;
	
	private final Map<Integer, Integer> commits;
	
	public UserContribution(String email, Map<Integer, Integer> commits) {
		this.email = email;
		this.commits = commits;
	}

	public String getEmail() {
		return email;
	}

	public Map<Integer, Integer> getCommits() {
		return commits;
	}
	
}
