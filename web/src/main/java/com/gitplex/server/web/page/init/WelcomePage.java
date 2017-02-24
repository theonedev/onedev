package com.gitplex.server.web.page.init;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.account.overview.NewDepotPage;
import com.gitplex.server.web.page.account.overview.NewOrganizationPage;
import com.gitplex.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public class WelcomePage extends LayoutPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new ViewStateAwarePageLink<Void>("addOrganization", NewOrganizationPage.class, 
				NewOrganizationPage.paramsOf(getLoginUser())));
		
		add(new ViewStateAwarePageLink<Void>("addDepot", NewDepotPage.class, 
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
