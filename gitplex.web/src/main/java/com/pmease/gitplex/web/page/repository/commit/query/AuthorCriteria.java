package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class AuthorCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String author;
	
	public AuthorCriteria(String author) {
		this.author = author;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.authors().add(author);
	}

}
