package com.gitplex.server.web.page.init;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.gitplex.server.web.page.admin.user.NewUserPage;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.page.layout.NewProjectPage;

@SuppressWarnings("serial")
public class WelcomePage extends LayoutPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new BookmarkablePageLink<Void>("addProject", NewProjectPage.class));
		add(new BookmarkablePageLink<Void>("addUser", NewUserPage.class));
	}

	@Override
	protected boolean isPermitted() {
		return getLoginUser() != null;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WelcomeResourceReference()));
	}
	
}
