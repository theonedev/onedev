package com.pmease.gitplex.web.page.repository.home;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoHomePage extends RepositoryPage {

	public RepoHomePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getRepository().getFQN();
	}
	
}
