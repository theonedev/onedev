package com.pmease.gitplex.web.page.repository.commit.filters;

import com.pmease.commons.git.command.LogCommand;

public class BranchFilter extends CommitFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "branch";
	}

	@Override
	public FilterEditor newEditor(String id, boolean focus) {
		return new BranchEditor(id, this, focus);
	}

	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.revisions().addAll(getValues());
	}

}
