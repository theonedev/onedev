package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class BeforeCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String date;
	
	public BeforeCriteria(String date) {
		this.date = date;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.before(date);
	}

}
