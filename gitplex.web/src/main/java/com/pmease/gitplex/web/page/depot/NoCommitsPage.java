package com.pmease.gitplex.web.page.depot;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public class NoCommitsPage extends DepotPage {

	public NoCommitsPage(PageParameters params) {
		super(params);
		
		if (getDepot().git().hasCommits())
			throw new RestartResponseException(DepotFilePage.class, paramsOf(getDepot()));
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
		response.render(CssHeaderItem.forReference(new CssResourceReference(NoCommitsPage.class, "no-commits.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotFilePage.class, paramsOf(depot));
	}
	
}
