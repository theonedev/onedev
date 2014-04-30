package com.pmease.gitop.web.page.repository.source.contributors;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class ContributorsPage extends RepositoryPage {

	public ContributorsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getRepository() + " - contributors";
	}

}
