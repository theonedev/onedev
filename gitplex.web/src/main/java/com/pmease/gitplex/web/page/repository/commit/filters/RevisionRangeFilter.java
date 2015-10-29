package com.pmease.gitplex.web.page.repository.commit.filters;

import com.pmease.commons.git.command.LogCommand;

public class RevisionRangeFilter extends CommitFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "revision range";
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

	@Override
	public FilterEditor<?> newEditor(String id, FilterCallback callback) {
		return new RevisionRangeEditor(id, this);
	}

	@Override
	public void applyTo(LogCommand logCommand) {
		String value = getValue();
		if (value != null)
			logCommand.revisionRange(value);
	}

}
