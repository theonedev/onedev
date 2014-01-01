package com.pmease.gitop.web.page.account.setting.teams;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.common.wicket.component.vex.AjaxConfirmLink;
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
		Team team = getTeam();
		return team.isNew() ? "Create Team" : team.getOwner().getName() + "'s team " + team.getName();
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
		
		add(new Label("teamname", new PropertyModel<String>(teamModel, "name")));
		add(new BookmarkablePageLink<Void>("backlink", AccountTeamsPage.class,
				AccountTeamsPage.newParams(getAccount())));
		
		add(new BookmarkablePageLink<Void>("addlink", AddTeamPage.class,
				AddTeamPage.newParams(getAccount())));
		
		WebMarkupContainer teamstoggle = new WebMarkupContainer("teamstoggle");
		add(teamstoggle);
		
		IModel<List<Team>> teamsModel = new LoadableDetachableModel<List<Team>>() {

			@Override
			public List<Team> load() {
				Team current = getTeam();
				if (current.isNew()) {
					return Lists.newArrayList(getAccount().getTeams());
				} else {
					List<Team> teams = Lists.newArrayList();
					for (Team each : getAccount().getTeams()) {
						if (!Objects.equal(each, current)) {
							teams.add(each);
						}
					}
					
					return teams;
				}
			}
		};
		
		ListView<Team> teamsView = new ListView<Team>("teams", teamsModel) {

			@Override
			protected void populateItem(ListItem<Team> item) {
				Team team = item.getModelObject();
				AbstractLink link = new BookmarkablePageLink<Void>("editlink", EditTeamPage.class, EditTeamPage.newParams(team));
				link.add(new Label("name", team.getName()));
				item.add(link);
			}
			
		};
		add(teamsView);
		
		add(new AjaxConfirmLink<Void>("deleteteamlink", Model.of("Are you sure you want to delete this team?")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Team team = getTeam();
				Gitop.getInstance(TeamManager.class).delete(team);
				setResponsePage(AccountTeamsPage.class, AccountTeamsPage.newParams(getAccount()));
			}
			
		});
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
