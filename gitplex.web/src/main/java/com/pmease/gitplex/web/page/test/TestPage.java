package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.link.Link;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequestVerification;
import com.pmease.gitplex.core.manager.PullRequestVerificationManager;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				PullRequestVerificationManager verificationManager = GitPlex.getInstance(PullRequestVerificationManager.class);
				PullRequestVerification verification = verificationManager.load(1L);
				verificationManager.delete(verification);
			}
			
		});
	}

}
