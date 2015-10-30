package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.Date;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.gitplex.web.Constants;

public class BeforeFilter extends CommitFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return "commit before";
	}

	@Override
	public FilterEditor newEditor(String id, boolean focus) {
		return new BeforeEditor(id, this, focus);
	}

	@Override
	public void applyTo(LogCommand logCommand) {
		if (!getValues().isEmpty()) {
			Date date = Constants.DATE_FORMATTER.parseDateTime(getValues().get(0)).toDate();
			logCommand.before(date);
		}
	}

}
