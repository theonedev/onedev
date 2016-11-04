package com.gitplex.server.web.page.account.overview;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.core.entity.Account;
import com.gitplex.server.web.page.account.AccountLayoutPage;

@SuppressWarnings("serial")
public class AccountOverviewPage extends AccountLayoutPage {

	public AccountOverviewPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getAccount().isOrganization())
			add(new OrganizationOverviewPanel("content", accountModel));
		else
			add(new UserOverviewPanel("content", accountModel));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(AccountOverviewPage.class, paramsOf(account));
	}

}
