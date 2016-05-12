package com.pmease.gitplex.web.page.test;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccountLink("account", (Account)null));
	}

}
