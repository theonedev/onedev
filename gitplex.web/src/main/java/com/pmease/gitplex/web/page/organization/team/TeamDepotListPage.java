package com.pmease.gitplex.web.page.organization.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.TeamAuthorizationManager;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.depotchoice.AbstractDepotChoiceProvider;
import com.pmease.gitplex.web.component.depotchoice.DepotChoiceResourceReference;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;
import com.pmease.gitplex.web.page.organization.PrivilegeSelectionPanel;
import com.vaynberg.wicket.select2.Response;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class TeamDepotListPage extends TeamPage {

	private static final String ADD_DEPOT_PLACEHOLDER = "Select repository to authorize...";
	
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
		
		add(searchField = new ClearableTextField<String>("searchDepots", Model.of("")));
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
				return new PrivilegeSelectionPanel(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
						close();
						TeamDepotListPage.this.filterPrivilege = privilege;
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
				term = term.toLowerCase();
				for (Depot depot: getAccount().getDepots()) {
					if (depot.getName().toLowerCase().contains(term)) {
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
				
				Collections.sort(depots, new Comparator<Depot>() {

					@Override
					public int compare(Depot depot1, Depot depot2) {
						return depot1.getName().compareTo(depot2.getName());
					}
					
				});
				
				new ResponseFiller<Depot>(response).fill(depots, page, Constants.DEFAULT_PAGE_SIZE);
			}

		}, ADD_DEPOT_PLACEHOLDER) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder(ADD_DEPOT_PLACEHOLDER);
				getSettings().setFormatResult("gitplex.depotChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.depotChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.depotChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getAccount()));
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot selection) {
				TeamAuthorization authorization = new TeamAuthorization();
				authorization.setDepot(selection);
				authorization.setTeam(teamModel.getObject());
				GitPlex.getInstance(TeamAuthorizationManager.class).persist(authorization);
				target.add(depotsContainer);
				target.add(pagingNavigator);
				target.add(noDepotsContainer);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(DepotChoiceResourceReference.INSTANCE));
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
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";
				
				for (TeamAuthorization authorization: teamModel.getObject().getAuthorizations()) {
					if (authorization.getDepot().getName().toLowerCase().contains(searchInput)
							&& (filterPrivilege == null || filterPrivilege == authorization.getPrivilege())) {
						authorizations.add(authorization);
					}
				}
				
				Collections.sort(authorizations, new Comparator<TeamAuthorization>() {

					@Override
					public int compare(TeamAuthorization authorization1, TeamAuthorization authorization2) {
						return authorization1.getDepot().getName().compareTo(authorization2.getDepot().getName());
					}
					
				});
				return authorizations;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<TeamAuthorization> item) {
				TeamAuthorization authorization = item.getModelObject();

				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", DepotFilePage.class, 
						DepotFilePage.paramsOf(authorization.getDepot()));
				link.add(new Label("name", authorization.getDepot().getName()));
				item.add(link);
				
				WebMarkupContainer greaterPrivileges = new WebMarkupContainer("greaterPrivileges") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!SecurityUtils.getGreaterPrivileges(item.getModelObject()).isEmpty());
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
						if (!SecurityUtils.canManage(getAccount())) {
							add(AttributeAppender.append("disabled", "disabled"));
						}
					}

					@Override
					public String getAfterDisabledLink() {
						return null;
					}

					@Override
					public String getBeforeDisabledLink() {
						return null;
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setEnabled(SecurityUtils.canManage(getAccount()));
					}

					@Override
					protected Component newContent(String id) {
						return new PrivilegeSelectionPanel(id) {
							
							@Override
							protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
								close();
								TeamAuthorization authorization = item.getModelObject();
								authorization.setPrivilege(privilege);
								GitPlex.getInstance(TeamAuthorizationManager.class).persist(authorization);
								target.add(pagingNavigator);
								target.add(depotsContainer);
								target.add(noDepotsContainer);
							}

						};
					}
					
				});
				
				item.add(new AjaxLink<Void>("remove") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						setVisible(SecurityUtils.canManage(getAccount()) 
								&& !pendingRemovals.contains(item.getModelObject().getId()));
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
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isMemberOf(getAccount());
	}
	
}
