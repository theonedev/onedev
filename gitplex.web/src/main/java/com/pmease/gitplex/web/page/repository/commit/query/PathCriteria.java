package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class PathCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String path;
	
	public PathCriteria(String path) {
		this.path = path;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.paths().add(path);
	}

}
