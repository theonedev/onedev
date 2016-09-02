package com.pmease.gitplex.web.page.init;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.pmease.gitplex.web.page.account.overview.NewDepotPage;
import com.pmease.gitplex.web.page.account.overview.NewOrganizationPage;
import com.pmease.gitplex.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class WelcomePage extends LayoutPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new BookmarkablePageLink<Void>("addOrganization", NewOrganizationPage.class, 
				NewOrganizationPage.paramsOf(getLoginUser())));
		
		add(new BookmarkablePageLink<Void>("addDepot", NewDepotPage.class, 
				NewDepotPage.paramsOf(getLoginUser())));
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
