package com.pmease.gitop.web.page.account.setting.teams;

import com.pmease.gitop.core.model.Team;

@SuppressWarnings("serial")
public class AddTeamPage extends EditTeamPage {

	public AddTeamPage() {
	}
	
	@Override
	protected Team getTeam() {
		Team team = new Team();
		team.setOwner(getAccount());
		return team;
	}
}
