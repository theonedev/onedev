package com.pmease.gitplex.web.page.repository.info.code.contributors;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;

@SuppressWarnings("serial")
public class ContributorsPage extends RepositoryInfoPage {

	public ContributorsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getRepository() + " - contributors";
	}

}
