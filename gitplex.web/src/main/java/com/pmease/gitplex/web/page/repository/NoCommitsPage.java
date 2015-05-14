package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.overview.RepoOverviewPage;

@SuppressWarnings("serial")
public class NoCommitsPage extends RepositoryPage {

	public NoCommitsPage(PageParameters params) {
		super(params);
		
		if (getRepository().git().hasCommits())
			throw new RestartResponseException(RepoOverviewPage.class, paramsOf(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("url1", getRepository().getUrl()));
		add(new Label("url2", getRepository().getUrl()));
		add(new Label("url3", getRepository().getUrl()));
	}
	
}
