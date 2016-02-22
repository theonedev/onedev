package com.pmease.gitplex.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.annotation.TeamChoice;
import com.pmease.gitplex.core.model.Team;

@SuppressWarnings("serial")
public abstract class TeamAwareGateKeeper extends AbstractGateKeeper {
	
	private String teamName;
	
	@Editable(name="Choose Team")
	@TeamChoice(excludes={Team.ANONYMOUS, Team.LOGGEDIN})
	@NotNull
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	
	public Team getTeam() {
		return GitPlex.getInstance(Dao.class).load(Team.class, getTeamId());
	}

}
