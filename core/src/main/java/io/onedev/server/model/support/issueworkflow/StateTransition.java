package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;

import javax.annotation.Nullable;

public abstract class StateTransition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String newState;
	
	@Nullable
	public abstract IssueAction getAction();

	public String getNewState() {
		return newState;
	}

	public void setNewState(String newState) {
		this.newState = newState;
	}
	
}
