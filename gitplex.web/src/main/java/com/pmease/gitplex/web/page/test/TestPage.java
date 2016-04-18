package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				Depot depot = GitPlex.getInstance(DepotManager.class).load(1L);
				depot.getTagRefs();
			}
			
		});
	}

}
