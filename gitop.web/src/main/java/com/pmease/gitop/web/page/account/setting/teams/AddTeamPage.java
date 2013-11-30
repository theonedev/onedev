package com.pmease.gitop.web.page.account.setting.teams;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.model.Team;
import com.pmease.gitop.web.model.TeamModel;

@SuppressWarnings("serial")
public class AddTeamPage extends EditTeamPage {

	public AddTeamPage(PageParameters params) {
		super(params);
		
		this.accountModel = newAccountModel(params);
		Team team = new Team();
		team.setOwner(accountModel.getObject());
		this.teamModel = new TeamModel(team);
	}
}
