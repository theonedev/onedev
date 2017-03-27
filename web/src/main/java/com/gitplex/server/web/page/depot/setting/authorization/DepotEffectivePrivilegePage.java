package com.gitplex.server.web.page.depot.setting.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.OrganizationMembership;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.web.WebConstants;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.component.avatar.Avatar;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.privilegeselection.PrivilegeSelectionPanel;
import com.gitplex.server.web.page.account.collaborators.CollaboratorDepotListPage;
import com.gitplex.server.web.page.account.collaborators.CollaboratorPrivilegeSourcePage;
import com.gitplex.server.web.page.account.members.MemberEffectivePrivilegePage;
import com.gitplex.server.web.page.account.members.MemberPrivilegeSourcePage;
import com.gitplex.server.web.page.depot.setting.DepotSettingPage;
import com.gitplex.server.web.util.depotaccess.DepotAccess;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotEffectivePrivilegePage extends DepotSettingPage {

	private PageableListView<UserPermission> usersView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer usersContainer; 
	
	private WebMarkupContainer noUsersContainer;

	private DepotPrivilege filterPrivilege;
	
	private String searchInput;
	
	public DepotEffectivePrivilegePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		
		add(searchField = new TextField<String>("searchUsers", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(usersContainer);
				target.add(pagingNavigator);
				target.add(noUsersContainer);
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
				return new PrivilegeSelectionPanel(id, true, filterPrivilege) {

					@Override
					protected void onSelect(AjaxRequestTarget target, DepotPrivilege privilege) {
						closeDropdown();
						filterPrivilege = privilege;
						target.add(filterContainer);
						target.add(usersContainer);
						target.add(pagingNavigator);
						target.add(noUsersContainer);
					}

				};
			}
		});
		filterContainer.add(new AjaxLink<Void>("clear") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				filterPrivilege = null;
				target.add(filterContainer);
				target.add(usersContainer);
				target.add(pagingNavigator);
				target.add(noUsersContainer);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filterPrivilege != null);
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
		
		usersContainer.add(usersView = new PageableListView<UserPermission>("users", 
				new LoadableDetachableModel<List<UserPermission>>() {

			@Override
			protected List<UserPermission> load() {
				List<UserPermission> permissions = new ArrayList<>();
				
				Depot depot = depotModel.getObject();
				Collection<Account> users = new HashSet<>();
				for (OrganizationMembership membership: getAccount().getOrganizationMembers()) {
					users.add(membership.getUser());
				}
				for (UserAuthorization authorization: depot.getAuthorizedUsers()) {
					users.add(authorization.getUser());
				}
				for (Account user: users) {
					if (user.matches(searchInput)) {
						DepotPrivilege privilege = new DepotAccess(user, depot).getGreatestPrivilege();
						if (privilege != DepotPrivilege.NONE 
								&& (filterPrivilege == null || filterPrivilege == privilege)) {
							permissions.add(new UserPermission(user, privilege));
						}
					}
				}
				
				permissions.sort((permission1, permission2) 
						-> permission1.getUser().getDisplayName().compareTo(permission2.getUser().getDisplayName()));
				return permissions;
			}
			
		}, WebConstants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<UserPermission> item) {
				UserPermission permission = item.getModelObject();

				OrganizationMembership membership = 
						getAccount().getOrganizationMembersMap().get(permission.getUser());
				if (membership != null) {
					PageParameters params = MemberEffectivePrivilegePage.paramsOf(membership);
					Link<Void> link = new ViewStateAwarePageLink<Void>(
							"avatarLink", 
							MemberEffectivePrivilegePage.class, 
							params);
					link.add(new Avatar("avatar", permission.getUser()));
					item.add(link);
					
					link = new ViewStateAwarePageLink<Void>(
							"nameLink", 
							MemberEffectivePrivilegePage.class, 
							params);
					link.add(new Label("name", permission.getUser().getDisplayName()));
					item.add(link);

					params = MemberPrivilegeSourcePage.paramsOf(membership, depotModel.getObject());
					link = new ViewStateAwarePageLink<Void>(
							"privilegeLink", 
							MemberPrivilegeSourcePage.class, 
							params);
					link.add(new Label("privilege", permission.getPrivilege().toString()));
					item.add(link);
				} else {
					PageParameters params = CollaboratorDepotListPage.paramsOf(
							getAccount(), permission.getUser());
	 
					Link<Void> link = new ViewStateAwarePageLink<Void>(
							"avatarLink", 
							CollaboratorDepotListPage.class, 
							params);
					link.add(new Avatar("avatar", permission.getUser()));
					item.add(link);
					
					link = new ViewStateAwarePageLink<Void>(
							"nameLink", 
							CollaboratorDepotListPage.class, 
							params);
					link.add(new Label("name", permission.getUser().getDisplayName()));
					item.add(link);

					params = CollaboratorPrivilegeSourcePage.paramsOf(depotModel.getObject(), permission.getUser());
					link = new ViewStateAwarePageLink<Void>(
							"privilegeLink", 
							CollaboratorPrivilegeSourcePage.class, 
							params);
					link.add(new Label("privilege", permission.getPrivilege().toString()));
					item.add(link);
				}
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
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		if (depot.getAccount().isOrganization())
			setResponsePage(DepotEffectivePrivilegePage.class, DepotEffectivePrivilegePage.paramsOf(depot));
		else
			setResponsePage(DepotCollaboratorListPage.class, DepotCollaboratorListPage.paramsOf(depot));
	}
	
	private static class UserPermission {
		
		private final Account user;
		
		private final DepotPrivilege privilege;
		
		public UserPermission(Account user, DepotPrivilege privilege) {
			this.user = user;
			this.privilege = privilege;
		}

		public Account getUser() {
			return user;
		}

		public DepotPrivilege getPrivilege() {
			return privilege;
		}
		
	}

}
