package io.onedev.server.web.page.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.TeamChoice;

@Editable
public class TeamsEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> teams = new ArrayList<>();

	@Editable
	@TeamChoice
	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}
	
}
