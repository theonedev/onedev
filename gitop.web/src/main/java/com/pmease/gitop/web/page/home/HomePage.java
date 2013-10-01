package com.pmease.gitop.web.page.home;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.web.common.component.fileupload.FileUploadBar;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.account.AccountHomePage;
import com.pmease.gitop.web.page.project.ProjectHomePage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new FileUploadBar("upload"));
		
		add(new BookmarkablePageLink<>("accountLink", AccountHomePage.class, AccountHomePage.paramsOf(Gitop.getInstance(UserManager.class).getRootUser())));
		add(new BookmarkablePageLink<>("projectLink", ProjectHomePage.class, ProjectHomePage.paramsOf(Gitop.getInstance(ProjectManager.class).load(1L))));
	}
}
