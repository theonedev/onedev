package com.pmease.gitplex.web.page.repository.tags;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoTagsPage extends RepositoryPage {

	public RepoTagsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoTagsPage.class, paramsOf(repository));
	}
	
}
