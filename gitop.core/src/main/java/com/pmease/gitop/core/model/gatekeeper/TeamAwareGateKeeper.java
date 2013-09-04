package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;

public abstract class TeamAwareGateKeeper extends AbstractGateKeeper {
	
	private Long teamId;
	
	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	@Override
	public Object trim(Object context) {
		TeamManager teamManager = Gitop.getInstance(TeamManager.class);
		if (teamManager.lookup(getTeamId()) == null)
			return null;
		else
			return this;
	}

}
