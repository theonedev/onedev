package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
public abstract class TeamAwareGateKeeper extends ApprovalGateKeeper {
	
	private Long teamId;
	
	@Editable
	@NotNull
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}
	
	public Team getTeam() {
		return Gitop.getInstance(TeamManager.class).load(getTeamId());
	}

	@Override
	protected GateKeeper trim(Project project) {
		TeamManager teamManager = Gitop.getInstance(TeamManager.class);
		if (teamManager.get(getTeamId()) == null)
			return null;
		else
			return this;
	}

}
