package com.pmease.gitplex.web.page.depot;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.page.depot.overview.DepotOverviewPage;

@SuppressWarnings("serial")
public class NoBranchesPage extends DepotPage {

	public NoBranchesPage(PageParameters params) {
		super(params);
		
		if (getDepot().getDefaultBranch() != null)
			throw new RestartResponseException(DepotOverviewPage.class, DepotOverviewPage.paramsOf(getDepot()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("url1", getDepot().getUrl()));
		add(new Label("url2", getDepot().getUrl()));
		add(new Label("url3", getDepot().getUrl()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new NoBranchesResourceReference()));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotOverviewPage.class, DepotOverviewPage.paramsOf(depot));
	}
	
}
