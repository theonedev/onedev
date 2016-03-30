package com.pmease.gitplex.web.component;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public class AccountLink extends BookmarkablePageLink<Void> {

	private final PageParameters params;
	
	private final String name;
	
	public AccountLink(String id, Account account) {
		super(id, AccountOverviewPage.class);

		params = AccountPage.paramsOf(account);
		name = account.getDisplayName();
	}
	
	public AccountLink(String id, PersonIdent person) {
		super(id, AccountOverviewPage.class);
		
		Account account = GitPlex.getInstance(AccountManager.class).findByPerson(person);
		if (account != null) { 
			params = AccountPage.paramsOf(account);
			name = account.getDisplayName();
		} else {
			params = new PageParameters();
			name = person.getName();
		}
	}
	
	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(name);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (params.isEmpty()) {
			setEnabled(false);
			setBeforeDisabledLink("");
			setAfterDisabledLink("");
		}
	}

}
