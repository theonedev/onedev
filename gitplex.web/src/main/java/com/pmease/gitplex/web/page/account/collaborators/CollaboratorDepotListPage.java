package com.pmease.gitplex.web.page.account.collaborators;

import java.util.ArrayList;
import java.util.Collection;
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

import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.UserAuthorization;
import com.pmease.gitplex.core.manager.UserAuthorizationManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.depotchoice.AbstractDepotChoiceProvider;
import com.pmease.gitplex.web.component.depotchoice.DepotChoiceResourceReference;
import com.pmease.gitplex.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.pmease.gitplex.web.depotaccess.DepotAccess;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotCollaboratorListPage;
import com.vaynberg.wicket.select2.Response;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class CollaboratorDepotListPage extends CollaboratorPage {

	private PageableListView<UserAuthorization> depotsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer depotsContainer; 
	
	private WebMarkupContainer noDepotsContainer;
	
	private DepotPrivilege filterPrivilege;

	private Set<Long> pendingRemovals = new HashSet<>();
	
	public CollaboratorDepotListPage(PageParameters params) {
		super(params);
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
		
		add(new SelectToAddChoice<Depot>("addNew", new AbstractDepotChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Depot> response) {
				List<Depot> depots = new ArrayList<>();
				term = term.toLowerCase();
				for (Depot depot: getAccount().getDepots()) {
					if (depot.getName().toLowerCase().contains(term)) {
						boolean authorized = false;
						for (UserAuthorization authorization: collaboratorModel.getObject().getAuthorizedDepots()) {
							if (authorization.getDepot().equals(depot)) {
								authorized = true;
								break;
							}
						}
						if (!authorized)
							depots.add(depot);
					}
				}
				
				depots.sort((depot1, depot2) -> depot1.getName().compareTo(depot2.getName()));
				
				new ResponseFiller<Depot>(response).fill(depots, page, Constants.DEFAULT_PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add Repository...");
				getSettings().setFormatResult("gitplex.depotChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.depotChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.depotChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Depot selection) {
				UserAuthorization authorization = new UserAuthorization();
				authorization.setUser(collaboratorModel.getObject());
				authorization.setDepot(selection);
				GitPlex.getInstance(UserAuthorizationManager.class).persist(authorization);
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
		
		AjaxLink<Void> confirmRemoveLink;
		add(confirmRemoveLink = new AjaxLink<Void>("confirmRemove") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Collection<UserAuthorization> authorizations = new HashSet<>();
				UserAuthorizationManager authorizationManager = GitPlex.getInstance(UserAuthorizationManager.class);
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
		
		depotsContainer = new WebMarkupContainer("depots") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!depotsView.getModelObject().isEmpty());
			}
			
		};
		depotsContainer.setOutputMarkupPlaceholderTag(true);
		add(depotsContainer);
		
		depotsContainer.add(depotsView = new PageableListView<UserAuthorization>("depots", 
				new LoadableDetachableModel<List<UserAuthorization>>() {

			@Override
			protected List<UserAuthorization> load() {
				List<UserAuthorization> authorizations = new ArrayList<>();
				
				String searchInput = searchField.getInput();
				if (searchInput != null)
					searchInput = searchInput.toLowerCase().trim();
				else
					searchInput = "";

				for (UserAuthorization authorization: collaboratorModel.getObject().getAuthorizedDepots()) {
					Depot depot = authorization.getDepot();
					if (depot.getAccount().equals(getAccount()) 
							&& depot.getName().toLowerCase().contains(searchInput)) {
						if (authorization.getPrivilege() != DepotPrivilege.NONE 
								&& (filterPrivilege == null || filterPrivilege == authorization.getPrivilege())) {
							authorizations.add(authorization);
						}
					}
				}
				
				authorizations.sort((authorization1, authorization2) 
						-> authorization1.getDepot().getName().compareTo(authorization2.getDepot().getName()));
				return authorizations;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<UserAuthorization> item) {
				UserAuthorization authorization = item.getModelObject();

				Link<Void> link = new BookmarkablePageLink<Void>(
						"depotLink", 
						DepotCollaboratorListPage.class, 
						DepotCollaboratorListPage.paramsOf(authorization.getDepot()));
				link.add(new Label("name", authorization.getDepot().getName()));
				item.add(link);

				WebMarkupContainer greaterPrivileges = new WebMarkupContainer("greaterPrivileges") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						UserAuthorization authorization = item.getModelObject();
						DepotAccess depotAccess = new DepotAccess(authorization.getUser(), authorization.getDepot());
						setVisible(SecurityUtils.isGreater(depotAccess.getGreatestPrivilege(), authorization.getPrivilege()));
					}
					
				};
				
				greaterPrivileges.add(new BookmarkablePageLink<Void>("detail", 
						CollaboratorPrivilegeSourcePage.class, 
						CollaboratorPrivilegeSourcePage.paramsOf(authorization.getDepot(), authorization.getUser())));
				
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
								UserAuthorization authorization = item.getModelObject();
								authorization.setPrivilege(privilege);
								GitPlex.getInstance(UserAuthorizationManager.class).persist(authorization);
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
	}
	
}
