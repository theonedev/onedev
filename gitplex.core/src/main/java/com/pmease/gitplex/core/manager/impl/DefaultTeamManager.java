package com.pmease.gitplex.core.manager.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.component.Team;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

@Singleton
public class DefaultTeamManager implements TeamManager, DepotListener {

	@Transactional
	@Override
	public void delete(Account organization, String teamName) {
		if (!organization.getTeams().containsKey(teamName)) {
			throw new RuntimeException("Unable to find team to delete: " + teamName);
		}
		organization.getTeams().remove(teamName);
		for (Membership membership: organization.getUserMemberships()) {
			membership.getJoinedTeams().remove(teamName);
		}
		for (Depot depot: organization.getDepots()) {
			for (Iterator<GateKeeper> it = depot.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onTeamDelete(teamName))
					it.remove();
			}
		}
	}

	@Transactional
	@Override
	public void save(Account organization, Team team, String oldTeamName) {
		if (oldTeamName != null && !oldTeamName.equals(team.getName())) {
			if (organization.getTeams().remove(oldTeamName) == null) {
				throw new RuntimeException("Unable to find original team: " + oldTeamName);
			}
			if (organization.getTeams().put(team.getName(), team) != null) {
				throw new RuntimeException("Team with name '" + team.getName() + "' already exists");				
			}

			for (Membership membership: organization.getUserMemberships()) {
				LinkedHashSet<String> joinedTeams = new LinkedHashSet<>();
				for (String teamName: membership.getJoinedTeams()) {
					if (teamName.equals(oldTeamName))
						joinedTeams.add(team.getName());
					else
						joinedTeams.add(teamName);
				}
				membership.setJoinedTeams(joinedTeams);
			}
			
			for (Depot depot: organization.getDepots()) {
				for (GateKeeper gateKeeper: depot.getGateKeepers()) {
					gateKeeper.onTeamRename(oldTeamName, team.getName());
				}
			}
		} else {
			if (organization.getTeams().put(team.getName(), team) != null) {
				throw new RuntimeException("Team with name '" + team.getName() + "' already exists");
			}
		}
	}

	@Transactional
	@Override
	public void onDepotDelete(Depot depot) {
		for (Team team: depot.getOwner().getTeams().values()) {
			team.getAuthorizations().remove(depot.getName());
		}
	}

	@Transactional
	@Override
	public void onDepotRename(Depot renamedDepot, String oldName) {
		for (Team team: renamedDepot.getOwner().getTeams().values()) {
			Map<String, DepotPrivilege> authorizations = new LinkedHashMap<>();
			for (Map.Entry<String, DepotPrivilege> entry: team.getAuthorizations().entrySet()) {
				if (entry.getKey().equals(oldName))
					authorizations.put(renamedDepot.getName(), entry.getValue());
				else
					authorizations.put(entry.getKey(), entry.getValue());
			}
			team.setAuthorizations(authorizations);
		}
	}
	
	@Transactional
	@Override
	public void onDepotTransfer(Depot depot, Account oldOwner) {
		for (Team team: oldOwner.getTeams().values()) {
			team.getAuthorizations().remove(depot.getName());
		}
	}

}
