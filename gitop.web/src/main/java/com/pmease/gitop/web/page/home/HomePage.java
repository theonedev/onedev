package com.pmease.gitop.web.page.home;

import com.pmease.gitop.web.page.AbstractLayoutPage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
//		add(new BookmarkablePageLink<>("accountLink", AccountHomePage.class, AccountHomePage.paramsOf(Gitop.getInstance(UserManager.class).getRootUser())));
//		add(new BookmarkablePageLink<>("projectLink", ProjectHomePage.class, ProjectHomePage.paramsOf(Gitop.getInstance(ProjectManager.class).load(1L))));
	}
}
