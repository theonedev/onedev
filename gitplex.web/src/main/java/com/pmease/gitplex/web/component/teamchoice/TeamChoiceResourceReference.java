package com.pmease.gitplex.web.component.teamchoice;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class TeamChoiceResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final TeamChoiceResourceReference INSTANCE = new TeamChoiceResourceReference();
	
	private TeamChoiceResourceReference() {
		super(TeamChoiceResourceReference.class, "team-choice.js");
	}

}
