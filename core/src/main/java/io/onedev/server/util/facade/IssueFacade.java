package io.onedev.server.util.facade;

import io.onedev.server.model.Issue;

public class IssueFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final String number;
	
	private final String title;
	
	private final String state;
	
	public IssueFacade(Issue issue) {
		super(issue.getId());

		number = String.valueOf(issue.getNumber());
		title = issue.getTitle();
		state = issue.getState();
	}

	public String getNumber() {
		return number;
	}

	public String getTitle() {
		return title;
	}

	public String getState() {
		return state;
	}

}
