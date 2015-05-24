package com.pmease.gitplex.web.page.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.WebSession;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.repository.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;
import com.pmease.gitplex.web.page.repository.setting.general.GeneralSettingPage;
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
		
		Link<Void> accountLink = new BookmarkablePageLink<>("accountLink", AccountReposPage.class, paramsOf(getAccount()));
		accountLink.add(new Label("accountName", getAccount().getName()));
		add(accountLink);
		
		Link<Void> repoLink = new BookmarkablePageLink<>("repoLink", RepoTreePage.class, paramsOf(getRepository()));
		repoLink.add(new Label("repoName", getRepository().getName()));
		add(repoLink);
		
		final WebMarkupContainer nav = new WebMarkupContainer("repoNav");
		nav.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (WebSession.get().isMiniSidebar())
					return "mini nav";
				else
					return "nav";
			}
			
		}));
		nav.setOutputMarkupId(true);
		add(nav);
		
		nav.add(new AjaxLink<Void>("miniToggle") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				WebSession session = WebSession.get();
				session.setMiniSidebar(!session.isMiniSidebar());
				target.add(nav);
				target.focusComponent(null);
			}
			
		});
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new RepoTab(Model.of("Files"), "fa fa-fw fa-files-o", RepoTreePage.class));
		tabs.add(new RepoTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", RepoCommitsPage.class));
		tabs.add(new RepoTab(Model.of("Branches"), "fa fa-fw fa-ext fa-branch", RepoBranchesPage.class));
		tabs.add(new RepoTab(Model.of("Tags"), "fa fa-fw fa-tag", RepoTagsPage.class));
		tabs.add(new RepoTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", OpenRequestsPage.class));
		
		if (SecurityUtils.canManage(getRepository()))
			tabs.add(new RepoTab(Model.of("Setting"), "fa fa-fw fa-cog", GeneralSettingPage.class, RepoSettingPage.class));
		
		nav.add(new Tabbable("tabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepositoryPage.class, "repository.css")));
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
