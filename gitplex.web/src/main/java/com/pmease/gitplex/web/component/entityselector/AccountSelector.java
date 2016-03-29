package com.pmease.gitplex.web.component.entityselector;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public abstract class AccountSelector extends EntitySelector<Account> {

	public AccountSelector(String id, IModel<Collection<Account>> accountsModel, Long currentAccountId) {
		super(id, accountsModel, currentAccountId);
	}

	@Override
	protected String getUrl(Account entity) {
		PageParameters params = AccountOverviewPage.paramsOf(entity);
		return urlFor(AccountOverviewPage.class, params).toString();
	}

	@Override
	protected String getNotFoundMessage() {
		return "No accounts found";
	}

	@Override
	protected Component renderEntity(String componentId, IModel<Account> entityModel) {
		return new AccountItemPanel(componentId, entityModel);
	}

	@Override
	protected boolean matches(Account entity, String searchTerm) {
		return entity.matches(searchTerm);
	}

}
