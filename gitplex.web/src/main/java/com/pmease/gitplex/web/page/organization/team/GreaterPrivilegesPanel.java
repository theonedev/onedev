package com.pmease.gitplex.web.page.organization.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.depotaccess.DepotAccess;
import com.pmease.gitplex.web.page.organization.OrganizationResourceReference;
import com.pmease.gitplex.web.page.organization.member.MemberPrivilegeSourcePage;

@SuppressWarnings("serial")
abstract class GreaterPrivilegesPanel extends GenericPanel<TeamAuthorization> {

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
				Collections.sort(permissions, new Comparator<UserPermission>() {

					@Override
					public int compare(UserPermission permission1, UserPermission permission2) {
						return permission1.getUser().getDisplayName()
								.compareTo(permission2.getUser().getDisplayName());
					}
					
				});
				return permissions;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<UserPermission> item) {
				UserPermission permission = item.getModelObject();
				PageParameters params = MemberPrivilegeSourcePage.paramsOf(
						getAuthorization().getDepot().getAccount(), 
						permission.getUser().getName(), 
						getAuthorization().getDepot());
				Link<Void> link = new BookmarkablePageLink<Void>("avatarLink", 
						MemberPrivilegeSourcePage.class, params);
				link.add(new Avatar("avatar", permission.getUser()));
				item.add(link);
				link = new BookmarkablePageLink<Void>("nameLink", 
						MemberPrivilegeSourcePage.class, params);
				link.add(new Label("name", permission.getUser().getDisplayName()));
				item.add(link);
				
				item.add(new Label("privilege", permission.getPrivilege()));
			}
			
		});
	}

	private TeamAuthorization getAuthorization() {
		return getModelObject();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(OrganizationResourceReference.INSTANCE));
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
