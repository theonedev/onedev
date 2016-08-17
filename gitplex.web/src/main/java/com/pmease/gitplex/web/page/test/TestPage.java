package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("link") {

			@Override
			public void onClick() {
				PullRequest request = GitPlex.getInstance(PullRequestManager.class).load(1L);
				request.getLatestUpdate();
			}
			
		});
	}

}
