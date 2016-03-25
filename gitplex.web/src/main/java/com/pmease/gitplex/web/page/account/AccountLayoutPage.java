package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceProvider;
import com.pmease.gitplex.web.component.accountchoice.AccountSingleChoice;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.depots.DepotListPage;
import com.pmease.gitplex.web.page.account.depots.NewDepotPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.organization.collaborator.AccountCollaboratorListPage;
import com.pmease.gitplex.web.page.organization.member.MemberListPage;
import com.pmease.gitplex.web.page.organization.member.MemberPage;
import com.pmease.gitplex.web.page.organization.member.NewMembersPage;
import com.pmease.gitplex.web.page.organization.team.NewTeamPage;
import com.pmease.gitplex.web.page.organization.team.TeamEditPage;
import com.pmease.gitplex.web.page.organization.team.TeamListPage;
import com.pmease.gitplex.web.page.organization.team.TeamPage;
import com.pmease.gitplex.web.page.user.notifications.NotificationListPage;
import com.pmease.gitplex.web.page.user.organizations.NewOrganizationPage;
import com.pmease.gitplex.web.page.user.organizations.OrganizationListPage;
import com.pmease.gitplex.web.page.user.setting.PasswordEditPage;

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
		
		IModel<Account> accountModel = Model.of(getAccount());
		AccountSingleChoice accountChoice = new AccountSingleChoice("accountChoice", accountModel, 
				new AccountChoiceProvider());
		accountChoice.setRequired(true);
		
		accountChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				AccountLayoutPage.this.onSelect(target, accountModel.getObject());
			}
			
		});
		add(accountChoice);
		
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new AccountTab("Overview", "fa fa-fw fa-list-alt", AccountOverviewPage.class));
		tabs.add(new AccountTab("Repositories", "fa fa-ext fa-fw fa-repo", 
				DepotListPage.class, NewDepotPage.class));
		if (getAccount().isOrganization()) {
			if (SecurityUtils.isMemberOf(getAccount())) {
				tabs.add(new AccountTab("Members", "fa fa-fw fa-user", MemberListPage.class, 
						NewMembersPage.class, MemberPage.class));
				tabs.add(new AccountTab("Teams", "fa fa-fw fa-group", TeamListPage.class, TeamPage.class, 
						TeamEditPage.class, NewTeamPage.class));
			} 
			if (SecurityUtils.canManage(getAccount())) {
				tabs.add(new AccountTab("Outside Collaborators", "fa fa-fw fa-user", AccountCollaboratorListPage.class));
				tabs.add(new AccountTab("Setting", "fa fa-fw fa-cog", ProfileEditPage.class, AvatarEditPage.class));
			}
		} else {
			tabs.add(new AccountTab("Organizations", "fa fa-fw fa-sitemap", 
					OrganizationListPage.class, NewOrganizationPage.class));
			if (SecurityUtils.canManage(getAccount())) {
				tabs.add(new AccountTab("Collaborators", "fa fa-fw fa-user", AccountCollaboratorListPage.class));
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

	protected abstract void onSelect(AjaxRequestTarget target, Account account);
}
