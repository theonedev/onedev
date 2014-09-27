package com.pmease.gitplex.web.page.repository.pullrequest;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class ClosedRequestsPage extends RepositoryPage {

	public ClosedRequestsPage(PageParameters params) {
		super(params);

		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new RequestListPanel("content", false));
	}

}