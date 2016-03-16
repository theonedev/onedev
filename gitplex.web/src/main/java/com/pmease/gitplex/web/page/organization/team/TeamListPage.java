package com.pmease.gitplex.web.page.organization.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.StaleObjectStateException;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.component.Team;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountOverviewPage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class TeamListPage extends AccountLayoutPage {

	private static final int MAX_MEMBERS = 20;
	
	private PageableListView<Team> teamsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer teamsContainer; 
	
	private WebMarkupContainer noTeamsContainer; 
	
	public TeamListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected String getPageTitle() {
		return "Teams - " + getAccount();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new ClearableTextField<String>("searchTeams", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(teamsContainer);
				target.add(noTeamsContainer);
				target.add(pagingNavigator);
			}
			
		});
		
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
		
		teamsContainer.add(teamsView = new PageableListView<Team>("teams", new LoadableDetachableModel<List<Team>>() {

			@Override
			protected List<Team> load() {
				List<Team> teams = new ArrayList<>();
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (Team team: getAccount().getTeams().values()) {
					if ((team.getName().toLowerCase().contains(searchInput))) {
						teams.add(team);
					}
				}
				
				Collections.sort(teams, new Comparator<Team>() {

					@Override
					public int compare(Team team1, Team team2) {
						return team1.getName().compareTo(team2.getName());
					}
					
				});
				return teams;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Team> item) {
				Team team = item.getModelObject();
				
				Link<Void> link = new BookmarkablePageLink<>("link", TeamPage.class, 
						TeamPage.paramsOf(getAccount(), team)); 
				link.add(new Label("name", team.getName()));
				item.add(link);
						
				RepeatingView membersView = new RepeatingView("members");
				int count = 0;
				for (Membership membership: getAccount().getUserMemberships()) {
					if (membership.getJoinedTeams().contains(team.getName()) && count++<MAX_MEMBERS) {
						membersView.add(new AvatarLink(membersView.newChildId(), membership.getUser()));
					}
				}
				item.add(membersView);
				if (count > MAX_MEMBERS) {
					item.add(new Link<Void>("more") {
	
						@Override
						public void onClick() {
							setResponsePage(TeamPage.class, TeamPage.paramsOf(getAccount(), team));
						}
						
					});
				} else {
					item.add(new WebMarkupContainer("more").setVisible(false));
				}
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener(
								"Do you really want to delete team " + team.getName() + "?"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(getAccount()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						Account organization = getAccount();
						if (organization.getVersion() != accountVersion) {
							throw new StaleObjectStateException(Account.class.getName(), organization.getId());
						}
						GitPlex.getInstance(TeamManager.class).delete(organization, team.getName());
						target.add(teamsContainer);
						target.add(noTeamsContainer);
						target.add(pagingNavigator);
					}
					
				});
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", teamsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(teamsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		if (account.isOrganization())
			setResponsePage(TeamListPage.class, paramsOf(account));
		else
			setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
