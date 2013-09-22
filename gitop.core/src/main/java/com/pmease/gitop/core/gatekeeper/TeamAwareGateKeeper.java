package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;

@SuppressWarnings("serial")
public abstract class TeamAwareGateKeeper extends AbstractGateKeeper {
	
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
	public Object trim(Object context) {
		TeamManager teamManager = Gitop.getInstance(TeamManager.class);
		if (teamManager.get(getTeamId()) == null)
			return null;
		else
			return this;
	}

}
