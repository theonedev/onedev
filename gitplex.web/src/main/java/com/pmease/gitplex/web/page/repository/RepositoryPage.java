package com.pmease.gitplex.web.page.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.reposelector.RepositorySelector;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.repository.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;
import com.pmease.gitplex.web.page.repository.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.tags.RepoTagsPage;

@SuppressWarnings("serial")
public abstract class RepositoryPage extends AccountPage {

	private static final String PARAM_REPO = "repo";

	protected final IModel<Repository> repoModel;
	
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
		
		WebMarkupContainer accountAndRepo = new WebMarkupContainer("accountAndRepo");
		add(accountAndRepo);
		Link<Void> accountLink = new BookmarkablePageLink<>("accountLink", AccountReposPage.class, paramsOf(getAccount()));
		accountLink.add(new Label("accountName", getAccount().getName()));
		accountAndRepo.add(accountLink);
		
		Link<Void> repoLink = new BookmarkablePageLink<>("repoLink", RepoFilePage.class, paramsOf(getRepository()));
		repoLink.add(new Label("repoName", getRepository().getName()));
		accountAndRepo.add(repoLink);
		
		DropdownPanel repoChoice = new DropdownPanel("repoChoice") {

			@Override
			protected Component newContent(String id) {
				return new RepositorySelector(id, null) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Repository repository) {
						setResponsePage(getPage().getClass(), paramsOf(repository));
					}
					
				};
			}
			
		};
		add(repoChoice);
		WebMarkupContainer repoChoiceTrigger = new WebMarkupContainer("repoChoiceTrigger");
		repoChoiceTrigger.add(new DropdownBehavior(repoChoice).alignWithComponent(accountAndRepo, 0, 100, 0, 0, 6, true));
		add(repoChoiceTrigger);
		
		UrlManager urlManager = GitPlex.getInstance(UrlManager.class);
		Model<String> cloneUrlModel = Model.of(urlManager.urlFor(getRepository()));
		add(new TextField<String>("cloneUrl", cloneUrlModel));
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new RepoTab(Model.of("Files"), "fa fa-fw fa-file-text-o", RepoFilePage.class));
		tabs.add(new RepoTab(Model.of("Commits"), "fa fa-fw fa-ext fa-commit", RepoCommitsPage.class));
		tabs.add(new RepoTab(Model.of("Branches"), "fa fa-fw fa-ext fa-branch", RepoBranchesPage.class));
		tabs.add(new RepoTab(Model.of("Tags"), "fa fa-fw fa-tag", RepoTagsPage.class));
		tabs.add(new RepoTab(Model.of("Pull Requests"), "fa fa-fw fa-ext fa-branch-compare", OpenRequestsPage.class));
		
		if (SecurityUtils.canManage(getRepository()))
			tabs.add(new RepoTab(Model.of("Setting"), "fa fa-fw fa-cog", GeneralSettingPage.class, RepoSettingPage.class));
		
		add(new Tabbable("repoTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RepositoryPage.class, "repository.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RepositoryPage.class, "repository.css")));
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
