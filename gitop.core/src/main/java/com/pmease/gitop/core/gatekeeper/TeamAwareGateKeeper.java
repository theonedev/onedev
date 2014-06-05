package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.TeamChoice;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class TeamAwareGateKeeper extends ApprovalGateKeeper {
	
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
		return Gitop.getInstance(Dao.class).load(Team.class, getTeamId());
	}

	@Override
	protected GateKeeper trim(Repository repository) {
		if (Gitop.getInstance(Dao.class).get(Team.class, getTeamId()) == null)
			return null;
		else
			return this;
	}

}
