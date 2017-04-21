package com.gitplex.server.model.support.tagcreator;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.Team;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.TeamChoice;
import com.google.common.base.Preconditions;

@Editable(order=300, name="Specified Team")
public class SpecifiedTeam implements TagCreator {

	private static final long serialVersionUID = 1L;

	private String teamName;

	@Editable(name="Team")
	@TeamChoice
	@NotEmpty
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Override
	public String getNotMatchMessage(Depot depot, Account user) {
		TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
		Team team = Preconditions.checkNotNull(teamManager.find(depot.getAccount(), teamName));
		if (!team.getMembers().contains(user)) 
			return "This operation can only be performed by team: " + teamName;
		else 
			return null;
	}

}
