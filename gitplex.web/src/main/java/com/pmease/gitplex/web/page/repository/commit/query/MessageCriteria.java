package com.pmease.gitplex.web.page.repository.commit.query;

import com.pmease.commons.git.command.LogCommand;

public class MessageCriteria implements QueryCriteria {

	private static final long serialVersionUID = 1L;

	private final String message;
	
	public MessageCriteria(String message) {
		this.message = message;
	}
	
	@Override
	public void applyTo(LogCommand logCommand) {
		logCommand.messages().add(message);
	}

}
