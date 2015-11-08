package com.pmease.gitplex.web.page.repository.commit.query;

import java.io.Serializable;

import com.pmease.commons.git.command.LogCommand;

public interface QueryCriteria extends Serializable {
	void applyTo(LogCommand logCommand);
}
