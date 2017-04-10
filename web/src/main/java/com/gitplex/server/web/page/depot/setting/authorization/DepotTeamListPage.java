package com.gitplex.server.web.page.depot.setting.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.TeamAuthorizationManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.Team;
import com.gitplex.server.model.TeamAuthorization;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.greaterprivilege.GreaterPrivilegesPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.component.modal.ModalPanel;
import com.gitplex.server.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.server.web.component.select2.Response;
import com.gitplex.server.web.component.select2.ResponseFiller;
import com.gitplex.server.web.component.select2.SelectToAddChoice;
import com.gitplex.server.web.component.teamchoice.AbstractTeamChoiceProvider;
import com.gitplex.server.web.component.teamchoice.TeamChoiceResourceReference;
import com.gitplex.server.web.page.account.teams.TeamDepotListPage;
import com.gitplex.server.web.page.depot.setting.DepotSettingPage;
import com.gitplex.server.web.util.depotaccess.DepotAccess;

@SuppressWarnings("serial")
public class DepotTeamListPage extends DepotSettingPage {

	private ListView<TeamAuthorization> teamsView;
	
	private WebMarkupContainer teamsContainer; 
	
	private WebMarkupContainer noTeamsContainer;
	
	private DepotPrivilege filterPrivilege;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public DepotTeamListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer filterContainer = new WebMarkupContainer("filter");
		filterContainer.setOutputMarkupId(true);
		add(filterContainer);
		
		filterContainer.add(new DropdownLink("selection") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (filterPrivilege == null)
							return "Filter by privilege";
						else 
							return filterPrivilege.toString();
					}
					
				}));
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new PrivilegeSelectionPanel(id, false, filterPrivilege) {

					@Override
					protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
						dropdown.close();
						filterPrivilege = privilege;
						target.add(filterContainer);
						target.add(teamsContainer);
						target.add(noTeamsContainer);
					}

				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterPrivilege = null;
				target.add(filterContainer);
				target.add(teamsContainer);
				target.add(noTeamsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filterPrivilege != null);
			}
			
		});
		
		add(new SelectToAddChoice<Team>("addNew", new AbstractTeamChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Team> response) {
				List<Team> teams = new ArrayList<>();
				for (Team team: getAccount().getDefinedTeams()) {
					if (team.matches(term)) {
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
				
				Collections.sort(teams);
				
				new ResponseFiller<Team>(response).fill(teams, page, WebConstants.DEFAULT_PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Select team to authorize...");
				getSettings().setFormatResult("gitplex.server.teamChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.server.teamChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.server.teamChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Team selection) {
				TeamAuthorization authorization = new TeamAuthorization();
				authorization.setTeam(selection);
				authorization.setDepot(depotModel.getObject());
				GitPlex.getInstance(TeamAuthorizationManager.class).save(authorization);
				target.add(teamsContainer);
				target.add(noTeamsContainer);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new TeamChoiceResourceReference()));
			}
			
		});
		
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
		
		teamsContainer = new WebMarkupContainer("teams") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!teamsView.getModelObject().isEmpty());
			}
			
		};
		teamsContainer.setOutputMarkupPlaceholderTag(true);
		add(teamsContainer);
		
		teamsContainer.add(teamsView = new ListView<TeamAuthorization>("teams", 
				new LoadableDetachableModel<List<TeamAuthorization>>() {

			@Override
			protected List<TeamAuthorization> load() {
				List<TeamAuthorization> authorizations = new ArrayList<>();
				
				for (TeamAuthorization authorization: depotModel.getObject().getAuthorizedTeams()) {
					if (filterPrivilege == null || filterPrivilege == authorization.getPrivilege()) {
						authorizations.add(authorization);
					}
				}
				
				authorizations.sort((o1, o2)->o1.getTeam().getName().compareTo(o2.getTeam().getName()));
				return authorizations;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<TeamAuthorization> item) {
				TeamAuthorization authorization = item.getModelObject();

				Link<Void> link = new ViewStateAwarePageLink<Void>("link", TeamDepotListPage.class, 
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
					protected Component newContent(String id, ModalPanel modal) {
						return new GreaterPrivilegesPanel(id, item.getModel()) {

							@Override
							protected void onClose(AjaxRequestTarget target) {
								modal.close();
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
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new PrivilegeSelectionPanel(id, false, item.getModelObject().getPrivilege()) {
							
							@Override
							protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
								dropdown.close();
								TeamAuthorization authorization = item.getModelObject();
								authorization.setPrivilege(privilege);
								GitPlex.getInstance(TeamAuthorizationManager.class).save(authorization);
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
					builder.append("Grant repository privileges to teams");
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
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		if (depot.getAccount().isOrganization())
			setResponsePage(DepotTeamListPage.class, DepotTeamListPage.paramsOf(depot));
		else
			setResponsePage(DepotCollaboratorListPage.class, DepotCollaboratorListPage.paramsOf(depot));
	}
	
}
