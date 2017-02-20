package com.gitplex.server.gatekeeper;

import java.util.Collection;
import java.util.HashSet;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.Team;
import com.gitplex.server.model.TeamMembership;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.TeamChoice;
import com.google.common.base.Preconditions;

public abstract class TeamAwareGateKeeper extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;

	private String teamName;

    @Editable(name="Team", order=100)
    @TeamChoice
    @NotEmpty
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
		if (teamName.equals(oldName))
			teamName = newName;
	}

	@Override
	public boolean onTeamDelete(String teamName) {
		return this.teamName.equals(teamName);
	}

	protected Team getTeam(Account organization) {
		return Preconditions.checkNotNull(GitPlex.getInstance(TeamManager.class).find(organization, teamName));
	}
	
	protected Collection<Account> getTeamMembers(Account organization) {
    	Collection<Account> members = new HashSet<>();
        for (TeamMembership membership: getTeam(organization).getMemberships()) {
        	members.add(membership.getUser());
        }        
        return members;
	}
	
	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account originalAccount) {
		return depotDefiningGateKeeper.equals(transferredDepot);
	}
	
}
