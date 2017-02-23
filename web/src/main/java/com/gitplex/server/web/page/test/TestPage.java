package com.gitplex.server.web.page.test;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.page.base.BasePage;
import com.gitplex.server.web.page.depot.file.DepotFilePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Depot depot = GitPlex.getInstance(DepotManager.class).load(1L);
		DepotFilePage.State state = new DepotFilePage.State();
		state.blobIdent.revision = "master";
		state.blobIdent.path = "hello/world";
		PageParameters params = DepotFilePage.paramsOf(depot, state); 
		add(new BookmarkablePageLink<Void>("test", DepotFilePage.class, params));
	}

}
