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
import com.pmease.commons.wicket.component.select2.ResponseFiller;
import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.UserAuthorization;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.UserAuthorizationManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.accountchoice.AbstractAccountChoiceProvider;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceResourceReference;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.pmease.gitplex.web.depotaccess.DepotAccess;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorEffectivePrivilegePage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorPrivilegeSourcePage;
import com.pmease.gitplex.web.page.account.members.MemberEffectivePrivilegePage;
import com.pmease.gitplex.web.page.account.members.MemberPrivilegeSourcePage;
import com.vaynberg.wicket.select2.Response;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotCollaboratorListPage extends DepotAuthorizationPage {

	private PageableListView<UserAuthorization> usersView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer usersContainer; 
	
	private WebMarkupContainer noUsersContainer;
	
	private Set<Long> pendingRemovals = new HashSet<>();
	
	public DepotCollaboratorListPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
				target.add(usersContainer);
				target.add(noUsersContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!pendingRemovals.isEmpty());
			}
			
		});
		confirmRemoveLink.setOutputMarkupPlaceholderTag(true);
		
		add(new SelectToAddChoice<Account>("addNew", new AbstractAccountChoiceProvider() {

			@Override
			public void query(String term, int page, Response<Account> response) {
				List<Account> users = new ArrayList<>();
				term = term.toLowerCase();
				for (Account user: GitPlex.getInstance(AccountManager.class).allUsers()) {
					if (user.matches(term) && !user.equals(getAccount()) && !user.isAdministrator()) {
						boolean authorized = false;
						for (UserAuthorization authorization: depotModel.getObject().getAuthorizedUsers()) {
							if (authorization.getUser().equals(user)) {
								authorized = true;
								break;
							}
						}
						if (!authorized)
							users.add(user);
					}
				}
				
				Collections.sort(users, new Comparator<Account>() {

					@Override
					public int compare(Account user1, Account user2) {
						return user1.getDisplayName().compareTo(user2.getDisplayName());
					}
					
				});
				
				new ResponseFiller<Account>(response).fill(users, page, Constants.DEFAULT_PAGE_SIZE);
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				getSettings().setPlaceholder("Add collaborator...");
				getSettings().setFormatResult("gitplex.accountChoiceFormatter.formatResult");
				getSettings().setFormatSelection("gitplex.accountChoiceFormatter.formatSelection");
				getSettings().setEscapeMarkup("gitplex.accountChoiceFormatter.escapeMarkup");
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Account selection) {
				UserAuthorization authorization = new UserAuthorization();
				authorization.setUser(selection);
				authorization.setDepot(depotModel.getObject());
				GitPlex.getInstance(UserAuthorizationManager.class).persist(authorization);
				target.add(usersContainer);
				target.add(pagingNavigator);
				target.add(noUsersContainer);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				response.render(JavaScriptHeaderItem.forReference(AccountChoiceResourceReference.INSTANCE));
			}
			
		});
		
		usersContainer = new WebMarkupContainer("users") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!usersView.getModelObject().isEmpty());
			}
			
		};
		usersContainer.setOutputMarkupPlaceholderTag(true);
		add(usersContainer);
		
		usersContainer.add(usersView = new PageableListView<UserAuthorization>("users", 
				new LoadableDetachableModel<List<UserAuthorization>>() {

			@Override
			protected List<UserAuthorization> load() {
				List<UserAuthorization> authorizations = new ArrayList<>();
				
				for (UserAuthorization authorization: depotModel.getObject().getAuthorizedUsers()) {
					if (!authorization.getUser().equals(getAccount())) {
						authorizations.add(authorization);
					}
				}
				
				Collections.sort(authorizations, new Comparator<UserAuthorization>() {

					@Override
					public int compare(UserAuthorization authorization1, UserAuthorization authorization2) {
						return authorization1.getUser().getDisplayName()
								.compareTo(authorization2.getUser().getDisplayName());
					}
					
				});
				return authorizations;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<UserAuthorization> item) {
				UserAuthorization authorization = item.getModelObject();

				OrganizationMembership membership = 
						getAccount().getOrganizationMembersMap().get(authorization.getUser());
				if (membership != null) {
					Link<Void> link = new BookmarkablePageLink<Void>("avatarLink", MemberEffectivePrivilegePage.class, 
							MemberEffectivePrivilegePage.paramsOf(membership));
					link.add(new Avatar("avatar", authorization.getUser()));
					item.add(link);
					
					link = new BookmarkablePageLink<Void>("nameLink", MemberEffectivePrivilegePage.class, 
							MemberEffectivePrivilegePage.paramsOf(membership));
					link.add(new Label("name", authorization.getUser().getDisplayName()));
					item.add(link);
				} else {
					Link<Void> link = new BookmarkablePageLink<Void>("avatarLink", CollaboratorEffectivePrivilegePage.class, 
							CollaboratorEffectivePrivilegePage.paramsOf(getAccount(), authorization.getUser()));
					link.add(new Avatar("avatar", authorization.getUser()));
					item.add(link);
					
					link = new BookmarkablePageLink<Void>("nameLink", CollaboratorEffectivePrivilegePage.class, 
							CollaboratorEffectivePrivilegePage.paramsOf(getAccount(), authorization.getUser()));
					link.add(new Label("name", authorization.getUser().getDisplayName()));
					item.add(link);
				}
				
				WebMarkupContainer greaterPrivileges = new WebMarkupContainer("greaterPrivileges") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						UserAuthorization authorization = item.getModelObject();
						DepotAccess depotAccess = new DepotAccess(authorization.getUser(), depotModel.getObject());
						setVisible(SecurityUtils.isGreater(depotAccess.getGreatestPrivilege(), authorization.getPrivilege()));
					}
					
				};
				
				if (membership != null) {
					greaterPrivileges.add(new BookmarkablePageLink<Void>("detail", 
							MemberPrivilegeSourcePage.class, 
							MemberPrivilegeSourcePage.paramsOf(membership, getDepot())));
				} else {
					greaterPrivileges.add(new BookmarkablePageLink<Void>("detail", 
							CollaboratorPrivilegeSourcePage.class, 
							CollaboratorPrivilegeSourcePage.paramsOf(authorization.getUser(), getDepot())));
				}
				
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
								target.add(usersContainer);
								target.add(noUsersContainer);
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

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("pageNav", usersView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(usersView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		noUsersContainer = new WebMarkupContainer("noUsers") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(usersView.getModelObject().isEmpty());
			}
			
		};
		noUsersContainer.setOutputMarkupPlaceholderTag(true);
		add(noUsersContainer);
	}
	
	@Override
	protected String getPageTitle() {
		return "Collaborators - " + getDepot();
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotCollaboratorListPage.class, DepotCollaboratorListPage.paramsOf(depot));
	}
	
}
