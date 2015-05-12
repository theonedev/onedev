package com.pmease.gitplex.web.page.repository;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public abstract class RepositoryPage extends AccountPage {

	private static final String PARAM_REPO = "repo";

	protected IModel<Repository> repoModel;
	
	public static PageParameters paramsOf(Repository repository) {
		PageParameters params = paramsOf(repository.getUser());
		params.set(PARAM_REPO, repository.getName());
		return params;
	}
	
	public RepositoryPage(PageParameters params) {
		super(params);
		
		String repositoryName = params.get(PARAM_REPO).toString();
		Preconditions.checkNotNull(repositoryName);
		
		if (repositoryName.endsWith(Constants.DOT_GIT_EXT)) {
			repositoryName = repositoryName.substring(0, 
					repositoryName.length() - Constants.DOT_GIT_EXT.length());
		}
		
		Repository repository = GitPlex.getInstance(RepositoryManager.class).findBy(getAccount(), repositoryName);
		
		if (repository == null) 
			throw new EntityNotFoundException("Unable to find repository " + getAccount() + "/" + repositoryName);
		
		repoModel = new RepositoryModel(repository);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(
				Permission.ofRepositoryRead(repoModel.getObject()));
	}
	
	public Repository getRepository() {
		return repoModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

}
