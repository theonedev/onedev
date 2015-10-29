package com.pmease.gitplex.web.page.repository.commit.filters;

import com.pmease.commons.git.command.LogCommand;

public class MessageFilter extends CommitFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "commit message";
	}

	@Override
	public FilterEditor newEditor(String id, boolean focus) {
		return new MessageEditor(id, this, focus);
	}

	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.messages(getValues());
	}

}
