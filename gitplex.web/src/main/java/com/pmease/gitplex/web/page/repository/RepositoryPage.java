package com.pmease.gitplex.web.page.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.repository.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;
import com.pmease.gitplex.web.page.repository.setting.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.repository.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.setting.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.repository.tags.RepoTagsPage;
import com.pmease.gitplex.web.page.repository.tree.RepoTreePage;

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
		
		String repoName = params.get(PARAM_REPO).toString();
		Preconditions.checkNotNull(repoName);
		
		if (repoName.endsWith(Constants.DOT_GIT_EXT))
			repoName = repoName.substring(0, repoName.length() - Constants.DOT_GIT_EXT.length());
		
		Repository repository = GitPlex.getInstance(RepositoryManager.class).findBy(getAccount(), repoName);
		
		if (repository == null) 
			throw new EntityNotFoundException("Unable to find repository " + getAccount() + "/" + repoName);
		
		repoModel = new RepositoryModel(repository);
		
		if (!(this instanceof NoCommitsPage) 
				&& !(this instanceof RepoSettingPage) 
				&& !getRepository().git().hasCommits()) { 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new RepoTab(Model.of("Files"), RepoTreePage.class));
		tabs.add(new RepoTab(Model.of("Commits"), RepoCommitsPage.class));
		tabs.add(new RepoTab(Model.of("Branches"), RepoBranchesPage.class));
		tabs.add(new RepoTab(Model.of("Tags"), RepoTagsPage.class));
		tabs.add(new RepoTab(Model.of("Pull Requests"), OpenRequestsPage.class));
		
		if (SecurityUtils.canManage(getRepository())) {
			tabs.add(new RepoTab(Model.of("Setting"), GeneralSettingPage.class, 
					GateKeeperPage.class, IntegrationPolicyPage.class));
		}
		
		add(new Tabbable("tabs", tabs));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepoPull(repoModel.getObject()));
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
