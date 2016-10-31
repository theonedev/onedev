package com.gitplex.web.page.account.teams;

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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.Team;
import com.gitplex.core.entity.TeamAuthorization;
import com.gitplex.core.manager.TeamAuthorizationManager;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.core.security.privilege.DepotPrivilege;
import com.gitplex.web.Constants;
import com.gitplex.web.component.depotchoice.AbstractDepotChoiceProvider;
import com.gitplex.web.component.depotchoice.DepotChoiceResourceReference;
import com.gitplex.web.component.greaterprivilege.GreaterPrivilegesPanel;
import com.gitplex.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.web.depotaccess.DepotAccess;
import com.gitplex.web.page.depot.setting.authorization.DepotTeamListPage;
import com.google.common.base.Preconditions;
import com.gitplex.commons.wicket.behavior.OnTypingDoneBehavior;
import com.gitplex.commons.wicket.component.DropdownLink;
import com.gitplex.commons.wicket.component.modal.ModalLink;
import com.gitplex.commons.wicket.component.select2.Response;
import com.gitplex.commons.wicket.component.select2.ResponseFiller;
import com.gitplex.commons.wicket.component.select2.SelectToAddChoice;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class TeamDepotListPage extends TeamPage {

	private PageableListView<TeamAuthorization> depotsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer depotsContainer; 
	
	private WebMarkupContainer noDepotsContainer;
	
	private DepotPrivilege filterPrivilege;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public TeamDepotListPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("searchDepots", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(depotsContainer);
				target.add(pagingNavigator);
				target.add(noDepotsContainer);
			}
			
		});
		
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
			protected Component newContent(String id) {
				return new PrivilegeSelectionPanel(id, false, filterPrivilege) {

					@Override
					protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
						close();
						filterPrivilege = privilege;
						target.add(filterContainer);
						target.add(depotsContainer);
						target.add(pagingNavigator);
						target.add(noDepotsContainer);
					}

				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterPrivilege = null;
				target.add(filterContainer);
				target.add(depotsContainer);
				target.add(pagingNavigator);
				target.add(noDepotsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filterPrivilege != null);
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
				target.add(pagingNavigator);
				target.add(depotsContainer);
				target.add(noDepotsContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!pendingRemovals.isEmpty());
			}
			
		});
		confirmRemoveLink.setOutputMarkupPlaceholderTag(true);
		
		add(new SelectToAddChoice<Depot>("addNew", new AbstractDepotChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Depot> response) {
				List<Depot> depots = new ArrayList<>();
				for (Depot depot: getAccount().getDepots()) {
					if (depot.matches(term)) {
						boolean authorized = false;
						for (TeamAuthorization authorization: teamModel.getObject().getAuthorizations()) {
							if (authorization.getDepot().equals(depot)) {
								authorized = true;
								break;
							}
						}
						if (!authorized)
							depots.add(depot);
					}
				}
				
				Collections.sort(depots);
				
				new ResponseFiller<Depot>(response).fill(depots, page, Constants.DEFAULT_PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add repository...");
				getSettings().setFormatResult("gitplex.depotChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.depotChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.depotChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Depot selection) {
				TeamAuthorization authorization = new TeamAuthorization();
				authorization.setDepot(selection);
				authorization.setTeam(teamModel.getObject());
				GitPlex.getInstance(TeamAuthorizationManager.class).save(authorization);
				target.add(depotsContainer);
				target.add(pagingNavigator);
				target.add(noDepotsContainer);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(new DepotChoiceResourceReference()));
			}
			
		});
		
		depotsContainer = new WebMarkupContainer("depots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!depotsView.getModelObject().isEmpty());
			}
			
		};
		depotsContainer.setOutputMarkupPlaceholderTag(true);
		add(depotsContainer);
		
		depotsContainer.add(depotsView = new PageableListView<TeamAuthorization>("depots", 
				new LoadableDetachableModel<List<TeamAuthorization>>() {

			@Override
			protected List<TeamAuthorization> load() {
				List<TeamAuthorization> authorizations = new ArrayList<>();
				
				for (TeamAuthorization authorization: teamModel.getObject().getAuthorizations()) {
					if (authorization.getDepot().matches(searchField.getInput())
							&& (filterPrivilege == null || filterPrivilege == authorization.getPrivilege())) {
						authorizations.add(authorization);
					}
				}
				
				authorizations.sort((authorization1, authorization2) 
						-> authorization1.getDepot().getName().compareTo(authorization2.getDepot().getName()));
				return authorizations;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<TeamAuthorization> item) {
				TeamAuthorization authorization = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>("link", DepotTeamListPage.class, 
						DepotTeamListPage.paramsOf(authorization.getDepot()));
				link.add(new Label("name", authorization.getDepot().getName()));
				item.add(link);
				
				WebMarkupContainer greaterPrivileges = new WebMarkupContainer("greaterPrivileges") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						boolean hasGreaterPrivileges = false;
						Team team = teamModel.getObject();
						TeamAuthorization authorization = item.getModelObject();
						for (Account user: team.getMembers()) {
							DepotAccess depotAccess = new DepotAccess(user, authorization.getDepot());
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
								GitPlex.getInstance(TeamAuthorizationManager.class).save(authorization);
								target.add(pagingNavigator);
								target.add(depotsContainer);
								target.add(noDepotsContainer);
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

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", depotsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		noDepotsContainer = new WebMarkupContainer("noDepots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(depotsView.getModelObject().isEmpty());
			}
			
		};
		noDepotsContainer.setOutputMarkupPlaceholderTag(true);
		add(noDepotsContainer);
		
		add(new Label("tip", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuilder builder = new StringBuilder("<i class='fa fa-info-circle'></i> ");
				if (getAccount().getDefaultPrivilege() == DepotPrivilege.NONE) {
					builder.append("Grant repository privileges here for the team");
				} else {
					builder.append("Grant extra repository privileges here for the team besides the "
							+ "default <b>" + getAccount().getDefaultPrivilege() + "</b> privilege "
							+ "in organization setting");
				}
				return builder.toString();
			}
			
		}).setEscapeModelStrings(false));
		
	}
	
	/*
	 * Team authorization page is only visible to administrator as it contains repository 
	 * authorization information and we do not want to expose that information to ordinary members 
	 * as repository name might also be a secret
	 */
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}
	
}
