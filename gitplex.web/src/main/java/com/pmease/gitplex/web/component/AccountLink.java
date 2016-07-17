package com.pmease.gitplex.web.component;

import javax.annotation.Nullable;

import org.apache.wicket.markup.ComponentTag;
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
	
	public AccountLink(String id, @Nullable Account account) {
		super(id, AccountOverviewPage.class);

		if (account != null) {
			params = AccountPage.paramsOf(account);
			name = account.getDisplayName();
		} else {
			params = new PageParameters();
			name = null;
		}
	}
	
	public AccountLink(String id, PersonIdent person) {
		super(id, AccountOverviewPage.class);
		name = person.getName();
		Account account = GitPlex.getInstance(AccountManager.class).find(person);
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
		if (name != null)
			return Model.of(name);
		else
			return Model.of("Unknown");
	}
	
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (params.isEmpty())
			tag.setName("span");
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (params.isEmpty()) {
			setEnabled(false);
		}
	}

}
