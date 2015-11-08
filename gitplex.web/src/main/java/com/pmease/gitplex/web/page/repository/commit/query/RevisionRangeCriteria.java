package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class RevisionRangeCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String revisionRange;
	
	public RevisionRangeCriteria(String revisionRange) {
		this.revisionRange = revisionRange;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.revisions().add(revisionRange);
	}

}
