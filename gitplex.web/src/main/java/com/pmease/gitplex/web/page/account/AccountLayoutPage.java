package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.collaborators.AccountCollaboratorListPage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorPage;
import com.pmease.gitplex.web.page.account.depots.DepotListPage;
import com.pmease.gitplex.web.page.account.depots.NewDepotPage;
import com.pmease.gitplex.web.page.account.members.MemberListPage;
import com.pmease.gitplex.web.page.account.members.MemberPage;
import com.pmease.gitplex.web.page.account.members.NewMembersPage;
import com.pmease.gitplex.web.page.account.notifications.NotificationListPage;
import com.pmease.gitplex.web.page.account.organizations.NewOrganizationPage;
import com.pmease.gitplex.web.page.account.organizations.OrganizationListPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.account.teams.TeamListPage;
import com.pmease.gitplex.web.page.account.teams.TeamPage;

@SuppressWarnings("serial")
public abstract class AccountLayoutPage extends AccountPage {
	
	public AccountLayoutPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BookmarkablePageLink<Void> avatarLink = new BookmarkablePageLink<Void>("avatar", 
				AvatarEditPage.class, AvatarEditPage.paramsOf(getAccount()));
		if (!SecurityUtils.canManage(getAccount()))
			avatarLink.setEnabled(false);
		add(avatarLink);
		avatarLink.add(new Avatar("avatar", accountModel.getObject(), null));
		
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new AccountTab("Overview", "fa fa-fw fa-list-alt", AccountOverviewPage.class));
		tabs.add(new AccountTab("Repositories", "fa fa-ext fa-fw fa-repo", 
				DepotListPage.class, NewDepotPage.class));
		
		if (getAccount().isOrganization()) {
			if (SecurityUtils.isMemberOf(getAccount())) {
				tabs.add(new AccountTab("Members", "fa fa-fw fa-user", 
						MemberListPage.class, NewMembersPage.class, MemberPage.class));
				tabs.add(new AccountTab("Teams", "fa fa-fw fa-group", TeamListPage.class, TeamPage.class));
			} 
			if (SecurityUtils.canManage(getAccount())) {
				tabs.add(new AccountTab("Collaborators", "fa fa-fw fa-user", 
						AccountCollaboratorListPage.class, CollaboratorPage.class));
				tabs.add(new AccountTab("Setting", "fa fa-fw fa-cog", ProfileEditPage.class, AvatarEditPage.class));
			} 
		} else {
			tabs.add(new AccountTab("Organizations", "fa fa-fw fa-group", 
					OrganizationListPage.class, NewOrganizationPage.class));
			if (SecurityUtils.canManage(getAccount())) {
				tabs.add(new AccountTab("Collaborators", "fa fa-fw fa-user", 
						AccountCollaboratorListPage.class, CollaboratorPage.class));
				tabs.add(new AccountTab("Notifications", "fa fa-fw fa-bell-o", NotificationListPage.class));
				tabs.add(new AccountTab("Setting", "fa fa-fw fa-cog", ProfileEditPage.class, 
						AvatarEditPage.class, PasswordEditPage.class));
			}
		}
		add(new Tabbable("accountTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(AccountLayoutPage.class, "account.css")));
	}

}
