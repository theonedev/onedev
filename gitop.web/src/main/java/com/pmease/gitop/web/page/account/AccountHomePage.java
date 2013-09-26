package com.pmease.gitop.web.page.account;

import org.apache.shiro.authz.annotation.RequiresAuthentication;

import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
@RequiresAuthentication
public class AccountHomePage extends AbstractLayoutPage {

	@Override
	protected String getPageTitle() {
		return "Gitop";
	}

}
