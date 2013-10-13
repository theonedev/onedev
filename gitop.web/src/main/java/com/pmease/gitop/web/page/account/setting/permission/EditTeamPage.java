package com.pmease.gitop.web.page.account.setting.permission;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.web.model.TeamModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class EditTeamPage extends AccountSettingPage {

	private final Long teamId;
	
	protected EditTeamPage() {
		teamId = null;
	}
	
	public EditTeamPage(PageParameters params) {
		Long teamId = params.get("teamId").toLongObject();
		this.teamId = teamId;
	}
	
	@Override
	protected Category getSettingCategory() {
		return Category.PERMISSION;
	}

	@Override
	protected String getPageTitle() {
		return "Edit Team";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new TeamEditor("editor", new TeamModel(getTeam())));
	}
	
	protected Team getTeam() {
		if (teamId == null) {
			throw new IllegalStateException("Team id cannot be null when editing team");
		} else {
			return Gitop.getInstance(TeamManager.class).get(teamId);
		}
	}
}
