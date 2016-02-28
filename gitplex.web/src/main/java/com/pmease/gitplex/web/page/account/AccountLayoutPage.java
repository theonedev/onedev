package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.userchoice.UserSingleChoice;
import com.pmease.gitplex.web.page.account.depots.AccountDepotsPage;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;

@SuppressWarnings("serial")
public abstract class AccountLayoutPage extends AccountPage {
	
	public AccountLayoutPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Avatar("accountAvatar", accountModel.getObject(), null));
		
		final IModel<Account> accountModel = Model.of(getAccount());
		UserSingleChoice accountChoice = new UserSingleChoice("accountChoice", accountModel, false);
		accountChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				setResponsePage(getPage().getClass(), paramsOf(accountModel.getObject()));
			}
			
		});
		add(accountChoice);
		
		add(new Link<Void>("runAsAccount") {

			@Override
			public void onClick() {
				SecurityUtils.getSubject().runAs(getAccount().getPrincipals());
				setResponsePage(getPage().getClass(), getPageParameters());
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				AccountManager userManager = GitPlex.getInstance(AccountManager.class);
				Account currentUser = userManager.getCurrent();
				setVisible(!getAccount().equals(currentUser) && SecurityUtils.canManage(getAccount()));
			}
			
		});
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AccountTab("Repositories", "fa fa-ext fa-fw fa-repo", AccountDepotsPage.class));
		
		if (SecurityUtils.canManage(getAccount())) {
			tabs.add(new AccountTab("Notifications", "fa fa-fw fa-bell-o", AccountNotificationsPage.class));
			tabs.add(new AccountTab("Setting", "fa fa-fw fa-cog", ProfileEditPage.class, 
					AvatarEditPage.class, PasswordEditPage.class));
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
