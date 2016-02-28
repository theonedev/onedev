package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.WebSession;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
				Account account = accountManager.load(1L);
				account.setPassword("");
				accountManager.save(account);
				WebSession.get().login("admin", "just do it", true);
			}
			
		});
	}

}
