package com.pmease.gitop.web.page.account.setting.teams;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.model.TeamModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class EditTeamPage extends AccountSettingPage {

	protected IModel<Team> teamModel;
	
	public static PageParameters newParams(Team team) {
		Preconditions.checkNotNull(team);
		PageParameters params = PageSpec.forUser(team.getOwner());
		params.set("teamId", team.getId());
		return params;
	}
	
	public EditTeamPage(PageParameters params) {
		super(params);
		
		User user = accountModel.getObject();
		
		StringValue sv = params.get("teamId");
		Team team;
		if (sv.isEmpty() || sv.isNull()) {
			team = new Team();
			team.setOwner(user);
		} else {
			Long id = sv.toLongObject();
			team = Gitop.getInstance(TeamManager.class).get(id);
			if (team == null) {
				throw new EntityNotFoundException("Team " + id + " doesn't exist");
			}
		}
		
		this.teamModel = new TeamModel(team);
	}
	
	@Override
	public boolean isPermitted() {
		Team team = getTeam();
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(team.getOwner()));
	}
	
	@Override
	protected String getPageTitle() {
		return "Edit Team";
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new TeamEditor("editor", new TeamModel(getTeam())));
		add(new Label("head", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getTeam().isNew() ? "Create Team" : "Edit Team";
			}
			
		}));
	}
	
	protected Team getTeam() {
		return teamModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (teamModel != null) {
			teamModel.detach();
		}
		
		super.onDetach();
	}
}
