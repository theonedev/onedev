package com.gitplex.server.web.page.account.overview;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.OrganizationMembership;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.AccountLink;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.roleselection.RoleSelectionPanel;

@SuppressWarnings("serial")
public class UserOverviewPanel extends GenericPanel<Account> {

	public UserOverviewPanel(String id, IModel<Account> model) {
		super(id, model);
	}

	private Account getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Account user = getUser();
		add(new Label("title", user.getDisplayName()));
		add(new Label("loginName", user.getName()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(user.getFullName()!=null);				
			}
			
		});
		add(new Label("email", user.getEmail())
				.add(AttributeAppender.append("href", "mailto:" + user.getEmail())));
		
		add(new BookmarkablePageLink<Void>("addOrganization", 
				NewOrganizationPage.class, NewOrganizationPage.paramsOf(getUser())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(user));				
			}
			
		});
		
		WebMarkupContainer organizationsContainer = new WebMarkupContainer("organizations") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getUser().getOrganizations().isEmpty());
			}

		};
		add(organizationsContainer);
		IModel<List<OrganizationMembership>> organizationsModel = new AbstractReadOnlyModel<List<OrganizationMembership>>() {

			@Override
			public List<OrganizationMembership> getObject() {
				List<OrganizationMembership> memberships = new ArrayList<>(getUser().getOrganizations());
				memberships.sort((o1, o2)->o1.getOrganization().getDisplayName().compareTo(o2.getOrganization().getDisplayName()));
				return memberships;
			}
			
		};
		organizationsContainer.add(new ListView<OrganizationMembership>("organizations", organizationsModel) {

			@Override
			protected void populateItem(ListItem<OrganizationMembership> item) {
				OrganizationMembership membership = item.getModelObject();
				item.add(new AvatarLink("avatarLink", membership.getOrganization()));
				item.add(new AccountLink("accountLink", membership.getOrganization()));
				item.add(new Label("role", membership.isAdmin()?RoleSelectionPanel.ROLE_ADMIN:RoleSelectionPanel.ROLE_MEMBER));
			}
			
		});
		
		add(new WebMarkupContainer("noOrganizations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getOrganizations().isEmpty());
			}
			
		});
		
		add(new DepotListPanel("depots", getModel()));
	}

}
