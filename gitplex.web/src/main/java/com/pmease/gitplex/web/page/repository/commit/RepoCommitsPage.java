package com.pmease.gitplex.web.page.repository.commit;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.GitPath;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoCommitsPage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	protected IModel<Repository> repoModel;
	
	protected String revision;
	
	public RepoCommitsPage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		
		revision = GitPath.normalize(params.get(PARAM_REVISION).toString());
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
	
}
