package com.pmease.gitop.web.page.project.source.contributors;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.web.page.project.RepositoryTabPage;

@SuppressWarnings("serial")
public class ContributorsPage extends RepositoryTabPage {

	public ContributorsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return getProject() + " - contributors";
	}

}
