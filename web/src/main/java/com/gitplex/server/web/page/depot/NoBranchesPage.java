package com.gitplex.server.web.page.depot;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.model.Depot;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;

@SuppressWarnings("serial")
public class NoBranchesPage extends DepotPage {

	public NoBranchesPage(PageParameters params) {
		super(params);
		
		if (getDepot().getDefaultBranch() != null)
			throw new RestartResponseException(DepotBlobPage.class, DepotBlobPage.paramsOf(getDepot()));
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
		setResponsePage(DepotBlobPage.class, DepotBlobPage.paramsOf(depot));
	}
	
}
