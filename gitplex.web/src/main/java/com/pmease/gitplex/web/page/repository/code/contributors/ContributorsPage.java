package com.pmease.gitplex.web.page.repository.code.contributors;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class ContributorsPage extends RepositoryPage {

	public ContributorsPage(PageParameters params) {
		super(params);

		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}

	@Override
	protected String getPageTitle() {
		return getRepository() + " - contributors";
	}

}
