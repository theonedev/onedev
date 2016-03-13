package com.pmease.gitplex.web.page.organization;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;

@SuppressWarnings("serial")
public class NewMemberPage extends AccountLayoutPage {

	public NewMemberPage(PageParameters params) {
		super(params);
		
		Preconditions.checkState(getAccount().isOrganization());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(NewMemberPage.class, "organization.css")));
	}

}
