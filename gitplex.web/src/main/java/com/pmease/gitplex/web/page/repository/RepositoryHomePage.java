package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;

@SuppressWarnings("serial")
public class RepositoryHomePage extends RepoTreePage {

	public RepositoryHomePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getRepository().getFullName();
	}
	
}
