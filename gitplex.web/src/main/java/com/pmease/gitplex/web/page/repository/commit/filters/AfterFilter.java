package com.pmease.gitplex.web.page.repository.commit.filters;

import java.util.Date;

import com.pmease.commons.git.command.LogCommand;

public class AfterFilter extends DateFilter {

	private static final long serialVersionUID = 1L;

	@Override
	protected void applyTo(LogCommand logCommand, Date date) {
		logCommand.after(date);
	}

	@Override
	public String getName() {
		return "commit after";
	}

}
