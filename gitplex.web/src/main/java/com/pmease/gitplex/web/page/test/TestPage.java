package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				PullRequest request = GitPlex.getInstance(Dao.class).load(PullRequest.class, 1L);
				GitPlex.getInstance(ReviewManager.class).findBy(request);
			}
			
		});
	}		

}
