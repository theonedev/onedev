package io.onedev.server.model.support.issueworkflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StateSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String OPEN = "open"; // reserved state
	
	public static final String CLOSED = "closed"; // reserved state
	
	private String name;
	
	private List<StateTransition> transitions = new ArrayList<>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<StateTransition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<StateTransition> transitions) {
		this.transitions = transitions;
	}

}
