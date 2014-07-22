package com.pmease.gitplex.web.page.account;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AccountPage extends LayoutPage {

	public static final String PARAM_USER = "user";
	
	protected final IModel<User> accountModel;
	
	public AccountPage(PageParameters params) {
		String name = params.get(PARAM_USER).toString();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		
		User user = GitPlex.getInstance(UserManager.class).findByName(name);
		if (user == null) {
			throw (new EntityNotFoundException("User " + name + " not found"));
		}
		
		accountModel = new UserModel(user);
	}

	@Override
	protected void onDetach() {
		if (accountModel != null) {
			accountModel.detach();
		}
		
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
