package com.gitplex.web.component.greaterprivilege;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.OrganizationMembership;
import com.gitplex.core.entity.TeamAuthorization;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.core.security.privilege.DepotPrivilege;
import com.gitplex.web.component.avatar.Avatar;
import com.gitplex.web.depotaccess.DepotAccess;
import com.gitplex.web.page.account.members.MemberEffectivePrivilegePage;
import com.gitplex.web.page.account.members.MemberPrivilegeSourcePage;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class GreaterPrivilegesPanel extends GenericPanel<TeamAuthorization> {

	public GreaterPrivilegesPanel(String id, IModel<TeamAuthorization> authorizationModel) {
		super(id, authorizationModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
		add(new Label("privilege", getAuthorization().getPrivilege()));
		add(new Label("depot", getAuthorization().getDepot().getName()));
		
		add(new ListView<UserPermission>("privileges", new LoadableDetachableModel<List<UserPermission>>() {

			@Override
			protected List<UserPermission> load() {
				List<UserPermission> permissions = new ArrayList<>();
				for (Account user: getAuthorization().getTeam().getMembers()) {
					DepotAccess access = new DepotAccess(user, getAuthorization().getDepot());
					if (SecurityUtils.isGreater(access.getGreatestPrivilege(), getAuthorization().getPrivilege())) {
						permissions.add(new UserPermission(user, access.getGreatestPrivilege()));
					}
				}
				permissions.sort((permission1, permission2) 
						-> permission1.getUser().compareTo(permission2.getUser()));
				return permissions;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<UserPermission> item) {
				UserPermission permission = item.getModelObject();
				Account organization = getAuthorization().getDepot().getAccount();
				OrganizationMembership membership = 
						Preconditions.checkNotNull(organization.getOrganizationMembersMap().get(permission.getUser()));
				PageParameters params = MemberEffectivePrivilegePage.paramsOf(membership);
				Link<Void> link = new BookmarkablePageLink<Void>("avatarLink", 
						MemberEffectivePrivilegePage.class, params);
				link.add(new Avatar("avatar", permission.getUser()));
				item.add(link);
				link = new BookmarkablePageLink<Void>("nameLink", 
						MemberEffectivePrivilegePage.class, params);
				link.add(new Label("name", permission.getUser().getDisplayName()));
				item.add(link);
				
				params = MemberPrivilegeSourcePage.paramsOf(
						membership, getAuthorization().getDepot());
				link = new BookmarkablePageLink<Void>("privilegeLink", 
						MemberPrivilegeSourcePage.class, params);
				link.add(new Label("privilege", permission.getPrivilege()));
				item.add(link);
			}
			
		});
	}

	private TeamAuthorization getAuthorization() {
		return getModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GreaterPrivilegesResourceReference()));
	}
	
	protected abstract void onClose(AjaxRequestTarget target);

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
