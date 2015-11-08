package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class AfterCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String date;
	
	public AfterCriteria(String date) {
		this.date = date;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.after(date);
	}

}
