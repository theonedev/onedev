package com.pmease.gitplex.web.page.repository.overview;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoOverviewPage extends RepositoryPage {

	public RepoOverviewPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getRepository().getFQN();
	}
	
}
