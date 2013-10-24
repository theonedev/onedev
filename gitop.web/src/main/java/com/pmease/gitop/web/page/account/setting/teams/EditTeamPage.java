package com.pmease.gitop.web.page.account.setting.teams;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.web.model.TeamModel;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class EditTeamPage extends AccountSettingPage {

	protected final Long teamId;
	
	public EditTeamPage() {
		this.teamId = null;
	}
	
	public EditTeamPage(PageParameters params) {
		this.teamId = params.get("teamId").toLongObject();
	}
	
	@Override
	protected Category getSettingCategory() {
		return Category.TEAMS;
	}

	@Override
	protected String getPageTitle() {
		return "Edit Team";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new TeamEditor("editor", new UserModel(getAccount()), new TeamModel(getTeam())));
		add(new Label("head", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getTeam().isNew() ? "Create Team" : "Edit Team";
			}
			
		}));
	}
	
	protected Team getTeam() {
		if (teamId == null) {
			throw new IllegalStateException("Team id cannot be null when editing team");
		} else {
			return Gitop.getInstance(TeamManager.class).get(teamId);
		}
	}
}
