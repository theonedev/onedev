package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public class NoCommitsPage extends RepositoryPage {

	public NoCommitsPage(PageParameters params) {
		super(params);
		
		if (getRepository().git().hasCommits())
			throw new RestartResponseException(RepoFilePage.class, paramsOf(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("url1", getRepository().getUrl()));
		add(new Label("url2", getRepository().getUrl()));
		add(new Label("url3", getRepository().getUrl()));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(NoCommitsPage.class, "no-commits.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoFilePage.class, paramsOf(repository));
	}
	
}
