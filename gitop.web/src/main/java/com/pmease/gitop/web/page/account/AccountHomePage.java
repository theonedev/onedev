package com.pmease.gitop.web.page.account;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.GeneralException;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
@RequiresAuthentication
public class AccountHomePage extends AbstractLayoutPage {

	private final IModel<User> accountModel;

	public AccountHomePage(PageParameters params) {
		String accountName = params.get("user").toString();
		
		User account = Gitop.getInstance(UserManager.class).find(accountName);
		if (account == null)
			throw new GeneralException("Account %s does not exist!", accountName);
		
		final Long accountId = account.getId();
		
		accountModel = new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return Gitop.getInstance(UserManager.class).load(accountId);
			}
			
		};

		add(new Label("accountName", getAccount().getName()));
		
		add(new Link<Void>("link") {

			@Override
			public void onClick() {
				
			}
			
		});
	}
	
	@Override
	protected String getPageTitle() {
		return "Gitop";
	}

	public User getAccount() {
		return accountModel.getObject();
	}
	
	@Override
	public void detachModels() {
		if (accountModel != null)
			accountModel.detach();
		
		super.detachModels();
	}

	public static PageParameters paramsOf(User account) {
		PageParameters params = new PageParameters();
		params.set("user", account.getName());
		return params;
	}
	
}
