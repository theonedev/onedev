package com.pmease.gitop.web.page.account.setting.teams;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.web.common.component.modal.Modal;
import com.pmease.gitop.web.common.component.vex.AjaxConfirmLink;
import com.pmease.gitop.web.model.TeamModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountTeamsPage extends AccountSettingPage {

	@Override
	protected Category getSettingCategory() {
		return Category.TEAMS;
	}

	@Override
	protected String getPageTitle() {
		return "Teams - " + getAccount();
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new BookmarkablePageLink<Void>("addTeam", AddTeamPage.class));
		add(newTeamsView());
	}
	
	private Component newTeamsView() {
		final WebMarkupContainer teamsDiv = new WebMarkupContainer("teams");
		teamsDiv.setOutputMarkupId(true);
		
		IModel<List<Team>> teamsModel = new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				List<Team> teams = Lists.newArrayList(getAccount().getTeams());
				return teams;
			}
		};
		
		ListView<Team> view = new ListView<Team>("team", teamsModel) {

			@Override
			protected void populateItem(ListItem<Team> item) {
				Team team = item.getModelObject();
				AbstractLink link = new BookmarkablePageLink<Void>("link", 
						EditTeamPage.class, 
						EditTeamPage.newParams(team));
				link.add(new Label("name", Model.of(team.getName())));
				item.add(link);
				item.add(new Label("members", team.getMemberships().size()));
				item.add(new Label("repos", team.getAuthorizations().size()));
				item.add(new Label("permission", team.getAuthorizedOperation().name()));
				item.add(new AjaxConfirmLink<Team>("removelink",
						new TeamModel(team),
						Model.of("Are you sure you want to remove team <b>" + team.getName() + "</b>?")) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Team p = (Team) this.getDefaultModelObject();
						Gitop.getInstance(TeamManager.class).delete(p);
						target.add(teamsDiv);
					}
				});
				
				item.add(new AjaxLink<Void>("copylink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						getModal().setContent(new Label(Modal.CONTENT_ID, "Hello"))
							.show(target);
					}
				});
			}
		};
		teamsDiv.add(view);
		return teamsDiv;
	}
}
