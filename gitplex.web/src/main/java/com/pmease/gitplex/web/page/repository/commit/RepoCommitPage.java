package com.pmease.gitplex.web.page.repository.commit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoCommitPage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	protected String revision;
	
	public RepoCommitPage(PageParameters params) {
		super(params);
		
		revision = GitUtils.normalizePath(params.get(PARAM_REVISION).toString());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
	public static PageParameters paramsOf(Repository repository, String revision) {
		PageParameters params = paramsOf(repository);
		params.set(PARAM_REVISION, revision);
		return params;
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoCommitsPage.class, paramsOf(repository));
	}
	
}
