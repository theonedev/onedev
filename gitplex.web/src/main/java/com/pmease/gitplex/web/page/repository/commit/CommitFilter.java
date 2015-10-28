package com.pmease.gitplex.web.page.repository.commit;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.command.LogCommand;

public interface CommitFilter extends Serializable {
	String getName();
	
	@Nullable
	FilterEditor<?> newEditor(String id);
	
	void applyTo(LogCommand logCommand);
	
	void applyTo(PageParameters params);
	
}
