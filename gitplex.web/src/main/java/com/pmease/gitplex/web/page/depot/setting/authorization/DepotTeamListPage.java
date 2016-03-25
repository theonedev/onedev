package com.pmease.gitplex.web.page.depot.setting.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.manager.TeamAuthorizationManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.greaterprivilege.GreaterPrivilegesPanel;
import com.pmease.gitplex.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.pmease.gitplex.web.component.teamchoice.AbstractTeamChoiceProvider;
import com.pmease.gitplex.web.component.teamchoice.TeamChoiceResourceReference;
import com.pmease.gitplex.web.depotaccess.DepotAccess;
import com.pmease.gitplex.web.page.organization.team.TeamDepotListPage;
import com.vaynberg.wicket.select2.Response;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotTeamListPage extends DepotAuthorizationPage {

	private PageableListView<TeamAuthorization> teamsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer teamsContainer; 
	
	private WebMarkupContainer noTeamsContainer;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public DepotTeamListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AjaxLink<Void> confirmRemoveLink;
		add(confirmRemoveLink = new AjaxLink<Void>("confirmRemove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Collection<TeamAuthorization> authorizations = new HashSet<>();
				TeamAuthorizationManager authorizationManager = GitPlex.getInstance(TeamAuthorizationManager.class);
				for (Long id: pendingRemovals) {
					authorizations.add(authorizationManager.load(id));
				}
				authorizationManager.delete(authorizations);
				pendingRemovals.clear();
				target.add(this);
				target.add(pagingNavigator);
				target.add(teamsContainer);
				target.add(noTeamsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!pendingRemovals.isEmpty());
			}
			
		});
		confirmRemoveLink.setOutputMarkupPlaceholderTag(true);
		
		add(new SelectToAddChoice<Team>("addNew", new AbstractTeamChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Team> response) {
				List<Team> teams = new ArrayList<>();
				term = term.toLowerCase();
				for (Team team: getAccount().getDefinedTeams()) {
					if (team.getName().toLowerCase().contains(term)) {
						boolean authorized = false;
						for (TeamAuthorization authorization: depotModel.getObject().getAuthorizedTeams()) {
							if (authorization.getTeam().equals(team)) {
								authorized = true;
								break;
							}
						}
						if (!authorized)
							teams.add(team);
					}
				}
				
				Collections.sort(teams, new Comparator<Team>() {

					@Override
					public int compare(Team team1, Team team2) {
						return team1.getName().compareTo(team2.getName());
					}
					
				});
				
				new ResponseFiller<Team>(response).fill(teams, page, Constants.DEFAULT_PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Select team to authorize...");
				getSettings().setFormatResult("gitplex.teamChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.teamChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.teamChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Team selection) {
				TeamAuthorization authorization = new TeamAuthorization();
				authorization.setTeam(selection);
				authorization.setDepot(depotModel.getObject());
				GitPlex.getInstance(TeamAuthorizationManager.class).persist(authorization);
				target.add(teamsContainer);
				target.add(pagingNavigator);
				target.add(noTeamsContainer);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(TeamChoiceResourceReference.INSTANCE));
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
		
		teamsContainer.add(teamsView = new PageableListView<TeamAuthorization>("teams", 
				new LoadableDetachableModel<List<TeamAuthorization>>() {

			@Override
			protected List<TeamAuthorization> load() {
				List<TeamAuthorization> authorizations = new ArrayList<>();
				
				for (TeamAuthorization authorization: depotModel.getObject().getAuthorizedTeams()) {
					authorizations.add(authorization);
				}
				
				Collections.sort(authorizations, new Comparator<TeamAuthorization>() {

					@Override
					public int compare(TeamAuthorization authorization1, TeamAuthorization authorization2) {
						return authorization1.getTeam().getName().compareTo(authorization2.getTeam().getName());
					}
					
				});
				return authorizations;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<TeamAuthorization> item) {
				TeamAuthorization authorization = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>("link", TeamDepotListPage.class, 
						TeamDepotListPage.paramsOf(authorization.getTeam()));
				link.add(new Label("name", authorization.getTeam().getName()));
				item.add(link);
				
				WebMarkupContainer greaterPrivileges = new WebMarkupContainer("greaterPrivileges") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						boolean hasGreaterPrivileges = false;
						TeamAuthorization authorization = item.getModelObject();
						Team team = authorization.getTeam();
						for (Account user: team.getMembers()) {
							DepotAccess depotAccess = new DepotAccess(user, depotModel.getObject());
							if (SecurityUtils.isGreater(depotAccess.getGreatestPrivilege(), authorization.getPrivilege())) {
								hasGreaterPrivileges = true;
								break;
							}
						}
						setVisible(hasGreaterPrivileges);
					}
					
				};
				greaterPrivileges.add(new ModalLink("detail") {

					@Override
					protected Component newContent(String id) {
						return new GreaterPrivilegesPanel(id, item.getModel()) {

							@Override
							protected void onClose(AjaxRequestTarget target) {
								close(target);
							}
							
						};
					}
					
				});
				item.add(greaterPrivileges);
				item.add(new DropdownLink("privilege") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return item.getModelObject().getPrivilege().toString();
							}
							
						}));
					}

					@Override
					protected Component newContent(String id) {
						return new PrivilegeSelectionPanel(id, false, item.getModelObject().getPrivilege()) {
							
							@Override
							protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
								close();
								TeamAuthorization authorization = item.getModelObject();
								authorization.setPrivilege(privilege);
								GitPlex.getInstance(TeamAuthorizationManager.class).persist(authorization);
								target.add(pagingNavigator);
								target.add(teamsContainer);
								target.add(noTeamsContainer);
								Session.get().success("Privilege updated");
							}

						};
					}
					
				});
				
				item.add(new AjaxLink<Void>("remove") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(!pendingRemovals.contains(item.getModelObject().getId()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						pendingRemovals.add(item.getModelObject().getId());
						target.add(item);
						target.add(confirmRemoveLink);
					}

				});
				item.add(new WebMarkupContainer("pendingRemoval") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(pendingRemovals.contains(item.getModelObject().getId()));
					}
					
				});
				item.add(new AjaxLink<Void>("undoRemove") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						pendingRemovals.remove(item.getModelObject().getId());
						target.add(item);
						target.add(confirmRemoveLink);
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(pendingRemovals.contains(item.getModelObject().getId()));
					}
					
				});
				item.setOutputMarkupId(true);
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
		
		noTeamsContainer = new WebMarkupContainer("noTeams") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(teamsView.getModelObject().isEmpty());
			}
			
		};
		noTeamsContainer.setOutputMarkupPlaceholderTag(true);
		add(noTeamsContainer);
		
		add(new Label("tip", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuilder builder = new StringBuilder("<i class='fa fa-info-circle'></i> ");
				if (getAccount().getDefaultPrivilege() == DepotPrivilege.NONE) {
					builder.append("Grant repository privileges here for teams");
				} else {
					builder.append("Grant extra repository privileges here for teams besides the "
							+ "default <b>" + getAccount().getDefaultPrivilege() + "</b> privilege "
							+ "in organization setting");
				}
				return builder.toString();
			}
			
		}).setEscapeModelStrings(false));
		
	}
	
	@Override
	protected String getPageTitle() {
		return "Team Authorizations - " + getDepot();
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		if (depot.getAccount().isOrganization())
			setResponsePage(DepotTeamListPage.class, DepotTeamListPage.paramsOf(depot));
		else
			setResponsePage(DepotCollaboratorListPage.class, DepotCollaboratorListPage.paramsOf(depot));
	}
	
}
