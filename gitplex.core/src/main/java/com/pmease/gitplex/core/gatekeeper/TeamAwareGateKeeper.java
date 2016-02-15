package com.pmease.gitplex.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.editable.TeamChoice;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.Team;

@SuppressWarnings("serial")
public abstract class TeamAwareGateKeeper extends AbstractGateKeeper {
	
	private Long teamId;
	
	@Editable(name="Choose Team")
	@TeamChoice(excludes={Team.ANONYMOUS, Team.LOGGEDIN})
	@NotNull
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}
	
	public Team getTeam() {
		return GitPlex.getInstance(Dao.class).load(Team.class, getTeamId());
	}

	@Override
	protected GateKeeper trim(Depot depot) {
		if (GitPlex.getInstance(Dao.class).get(Team.class, getTeamId()) == null)
			return null;
		else
			return this;
	}

}
