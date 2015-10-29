package com.pmease.gitplex.web.page.repository.commit.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.git.command.LogCommand;

public abstract class CommitFilter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> values = new ArrayList<>();
	
	public abstract String getName();

	@Nullable
	public abstract FilterEditor newEditor(String id, boolean focus);
	
	public abstract void applyTo(LogCommand logCommand);
	
	public List<String> getValues() {
		return values;
	}
	
	public void setValues(List<String> values) {
		this.values = values;
	}
	
}
