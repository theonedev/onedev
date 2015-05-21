package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AccountPage extends LayoutPage {
	
	private static final String PARAM_USER = "user";
	
	protected final IModel<User> accountModel;
	
	public AccountPage(PageParameters params) {
		super(params);
		
		String name = params.get(PARAM_USER).toString();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		
		User user = GitPlex.getInstance(UserManager.class).findByName(name);
		if (user == null) 
			throw (new EntityNotFoundException("User " + name + " not found"));
		
		accountModel = new UserModel(user);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AccountTab(Model.of("Overview"), AccountOverviewPage.class));
		tabs.add(new AccountTab(Model.of("Repositories"), AccountReposPage.class));
		
		if (SecurityUtils.canManage(getAccount())) {
			tabs.add(new AccountTab(Model.of("Notifications"), AccountNotificationsPage.class));
			tabs.add(new AccountTab(Model.of("Setting"), ProfileEditPage.class, 
					AvatarEditPage.class, PasswordEditPage.class));
		}
		add(new Tabbable("tabs", tabs));
	}

	@Override
	protected void onDetach() {
		accountModel.detach();
		
		super.onDetach();
	}
	
	public User getAccount() {
		return accountModel.getObject();
	}
	
	public static PageParameters paramsOf(User user) {
		PageParameters params = new PageParameters();
		params.set(PARAM_USER, user.getName());
		return params;
	}

}
