package com.gitplex.server.web.page.account.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.manager.TeamMembershipManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Team;
import com.gitplex.server.model.TeamMembership;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.page.account.AccountLayoutPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;
import com.gitplex.server.web.util.ajaxlistener.ConfirmListener;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class TeamListPage extends AccountLayoutPage {

	private static final int MAX_DISPLAY_MEMBERS = 20;
	
	private final IModel<Collection<TeamMembership>> teamMembershipsModel = 
			new LoadableDetachableModel<Collection<TeamMembership>>() {

		@Override
		protected Collection<TeamMembership> load() {
			TeamMembershipManager teamMembershipManager = GitPlex.getInstance(TeamMembershipManager.class);
			return teamMembershipManager.findAll(getAccount());
		}
		
	};
	
	private ListView<Team> teamsView;
	
	private WebMarkupContainer teamsContainer; 
	
	private WebMarkupContainer noTeamsContainer; 
	
	public TeamListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("addNew") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getAccount()));
			}

			@Override
			public void onClick() {
				setResponsePage(NewTeamPage.class, NewTeamPage.paramsOf(getAccount()));
			}
			
		});
		
		teamsContainer = new WebMarkupContainer("teams") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!teamsView.getModelObject().isEmpty());
			}
			
		};
		teamsContainer.setOutputMarkupPlaceholderTag(true);
		add(teamsContainer);
		
		noTeamsContainer = new WebMarkupContainer("noTeams") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(teamsView.getModelObject().isEmpty());
			}
			
		};
		noTeamsContainer.setOutputMarkupPlaceholderTag(true);
		add(noTeamsContainer);
		
		teamsContainer.add(teamsView = new ListView<Team>("teams", new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				List<Team> teams = new ArrayList<>(getAccount().getDefinedTeams());
				Collections.sort(teams);
				return teams;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Team> item) {
				Team team = item.getModelObject();
				
				Link<Void> link = new BookmarkablePageLink<>("link", TeamMemberListPage.class, 
						TeamMemberListPage.paramsOf(team)); 
				link.add(new Label("name", team.getName()));
				item.add(link);
						
				RepeatingView membersView = new RepeatingView("members");
				int count = 0;
				for (TeamMembership teamMembership: teamMembershipsModel.getObject()) {
					if (teamMembership.getTeam().equals(team) && count++<MAX_DISPLAY_MEMBERS) {
						WebMarkupContainer child = new WebMarkupContainer(membersView.newChildId());
						child.add(new AvatarLink("member", teamMembership.getUser(), new TooltipConfig()));
						membersView.add(child);
					}
				}
				item.add(membersView);
				if (count > MAX_DISPLAY_MEMBERS) {
					item.add(new Link<Void>("more") {
	
						@Override
						public void onClick() {
							setResponsePage(TeamMemberListPage.class, TeamMemberListPage.paramsOf(item.getModelObject()));
						}
						
					});
				} else {
					item.add(new WebMarkupContainer("more").setVisible(false));
				}
				
				Long teamId = team.getId();
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener(
								"Do you really want to delete team " + item.getModelObject().getName() + "?"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(getAccount()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						TeamManager teamManager = GitPlex.getInstance(TeamManager.class);
						teamManager.delete(teamManager.load(teamId));
						target.add(teamsContainer);
						target.add(noTeamsContainer);
					}
					
				});
			}
			
		});
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccess(getAccount());
	}
	
	@Override
	protected void onDetach() {
		teamMembershipsModel.detach();
		
		super.onDetach();
	}

}
