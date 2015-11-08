package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class CommitterCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String committer;
	
	public CommitterCriteria(String committer) {
		this.committer = committer;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.committers().add(committer);
	}

}
