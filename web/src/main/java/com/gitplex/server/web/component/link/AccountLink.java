package com.gitplex.server.web.component.link;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.web.page.account.AccountPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;

@SuppressWarnings("serial")
public class AccountLink extends ViewStateAwarePageLink<Void> {

	private final PageParameters params;
	
	private final String name;
	
	public AccountLink(String id, @Nullable Account account) {
		super(id, AccountOverviewPage.class);

		if (account == null) {
			params = new PageParameters();
			name = GitPlex.NAME;
		} else if (account.getId() == null) {
			params = new PageParameters();
			name = account.getDisplayName();
		} else {
			params = AccountPage.paramsOf(account);
			name = account.getDisplayName();
		}
	}
	
	public AccountLink(String id, PersonIdent person) {
		super(id, AccountOverviewPage.class);
		Account account = GitPlex.getInstance(AccountManager.class).find(person);
		name = person.getName();
		if (account != null) { 
			params = AccountPage.paramsOf(account);
		} else {
			params = new PageParameters();
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
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		configure();
		if (!isEnabled())
			tag.setName("span");
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEnabled(!params.isEmpty());
	}

}
