package com.pmease.gitop.web.page.home;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.web.common.component.fileupload.FileUploadBar;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.account.AccountHomePage;

public class HomePage extends AbstractLayoutPage {

	private static final long serialVersionUID = 1L;
	
	public HomePage() {
		add(new FileUploadBar("upload"));
		
		add(new BookmarkablePageLink<>("accountLink", AccountHomePage.class, AccountHomePage.paramsOf(Gitop.getInstance(UserManager.class).getRootUser())));
	}

	@Override
	protected String getPageTitle() {
		return "Gitop - Home";
	}

}
