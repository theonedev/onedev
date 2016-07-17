package com.pmease.gitplex.web.page.account;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AccountPage extends LayoutPage {
	
	private static final String PARAM_ACCOUNT = "account";
	
	protected final IModel<Account> accountModel;
	
	public AccountPage(PageParameters params) {
		super(params);
		
		String name = params.get(PARAM_ACCOUNT).toString();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
		
		Account account = GitPlex.getInstance(AccountManager.class).find(name);
		if (account == null) 
			throw (new EntityNotFoundException("Account " + name + " not found"));
		
		accountModel = new UserModel(account);
	}

	@Override
	protected void onDetach() {
		accountModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public Account getAccount() {
		return accountModel.getObject();
	}
	
	public static PageParameters paramsOf(Account user) {
		return paramsOf(user.getName());
	}

	public static PageParameters paramsOf(String accountName) {
		PageParameters params = new PageParameters();
		params.set(PARAM_ACCOUNT, accountName);
		return params;
	}
	
	@Override
	protected Component newContextHead(String componentId) {
		return new Label(componentId, getAccount().getDisplayName());
	}

}
