package com.pmease.gitplex.web.page.repository;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.git.PathUtils;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Authorization;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountBasePage;
import com.pmease.gitplex.web.page.account.AccountHomePage;
import com.pmease.gitplex.web.page.repository.admin.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.admin.PermissionSettingPage;
import com.pmease.gitplex.web.page.repository.admin.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.repository.admin.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.repository.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.code.branches.BranchComparePage;
import com.pmease.gitplex.web.page.repository.code.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.code.commits.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.code.contributors.ContributorsPage;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;
import com.pmease.gitplex.web.page.repository.pullrequest.ClosedRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestDetailPage;

@SuppressWarnings("serial")
public abstract class RepositoryPage extends AccountBasePage {

	public static final String PARAM_REPO = "repo";

	// below are commonly used params for various pages inherited from this class
	public static final String PARAM_REVISION = "revision";
	
	public static final String PARAM_PATH = "path";

	protected IModel<Repository> repoModel;
	
	protected final String currentRevision;
	
	protected final String currentPath;
	
	public static PageParameters paramsOf(Repository repository) {
		return paramsOf(repository, null);
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision) {
		return paramsOf(repository, revision, null);
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		PageParameters params = paramsOf(repository.getUser());
		params.set(PARAM_REPO, repository.getName());
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		return params;
	}
	
	public RepositoryPage(PageParameters params) {
		super(params);
		
		String repositoryName = params.get(PARAM_REPO).toString();
		Preconditions.checkNotNull(repositoryName);
		
		if (repositoryName.endsWith(Constants.DOT_GIT_EXT))
			repositoryName = repositoryName.substring(0, 
					repositoryName.length() - Constants.DOT_GIT_EXT.length());
		
		Repository repository = GitPlex.getInstance(RepositoryManager.class).findBy(getAccount(), repositoryName);
		
		if (repository == null) {
			throw new EntityNotFoundException("Unable to find repository " 
						+ getAccount() + "/" + repositoryName);
		}
		
		repoModel = new RepositoryModel(repository);
		currentRevision = PathUtils.normalize(params.get(PARAM_REVISION).toString());
		currentPath = PathUtils.normalize(params.get(PARAM_PATH).toString());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Repository repository = getRepository();
		if (repository.getForkedFrom() != null)
			add(new Icon("repoIcon", "fa-ext fa-repo-fork"));
		else
			add(new Icon("repoIcon", "fa-ext fa-repo"));
		
		AbstractLink userlink = new BookmarkablePageLink<Void>("userlink", 
				AccountHomePage.class, AccountHomePage.paramsOf(repository.getOwner()));
		add(userlink);
		userlink.add(new Label("name", Model.of(repository.getOwner().getName())));
		
		AbstractLink repositoryLink = new BookmarkablePageLink<Void>("repositorylink", 
				RepositoryHomePage.class, RepositoryHomePage.paramsOf(repository));
		add(repositoryLink);
		repositoryLink.add(new Label("name", Model.of(repository.getName())));
		
		Label publicLabel = new Label("public-label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return isPubliclyAccessible() ? "public" : "private";
			}
		}) {
			@Override
			public void onEvent(IEvent<?> event) {
				if (event.getPayload() instanceof RepositoryPubliclyAccessibleChanged) {
					RepositoryPubliclyAccessibleChanged e = (RepositoryPubliclyAccessibleChanged) event.getPayload();
					e.getTarget().add(this);
				}
			}
		};
		
		publicLabel.setOutputMarkupId(true);
		publicLabel.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return isPubliclyAccessible() ? "" : "hidden";
			}
			
		}));
		add(publicLabel);
		
		WebMarkupContainer actionsContainer = new WebMarkupContainer("repoActions") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getRepository().git().hasCommits());
			}
			
		};
		add(actionsContainer);
		
		actionsContainer.add(new Link<Void>("forkRepo") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				User currentUser = userManager.getCurrent();
				setVisible(getRepository().isForkable() 
						&& currentUser != null 
						&& !getRepository().getOwner().equals(currentUser));
			}

			@Override
			public void onClick() {
				User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
				Repository forked = GitPlex.getInstance(RepositoryManager.class).fork(getRepository(), currentUser);
				setResponsePage(RepositoryHomePage.class, paramsOf(forked));
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("adminRepo", 
				GeneralSettingPage.class, paramsOf(getRepository())) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository())));
			}
		});
		
		List<Tab> codeTabs = new ArrayList<>();
		codeTabs.add(new RepositoryTab(Model.of("Files"), "fa fa-file-code-o", RepoTreePage.class, RepoBlobPage.class));
		codeTabs.add(new RepositoryTab(Model.of("Commits"), "fa fa-ext fa-commit", RepoCommitsPage.class, RepoCommitPage.class));
		codeTabs.add(new RepositoryTab(Model.of("Branches"), "fa fa-ext fa-branch", RepoBranchesPage.class, BranchComparePage.class));
		codeTabs.add(new RepositoryTab(Model.of("Contributors"), "fa fa-group", ContributorsPage.class));
		
		add(new Tabbable("codeTabs", codeTabs));
		
		List<Tab> requestsTabs = new ArrayList<>();
		requestsTabs.add(new RepositoryTab(Model.of("Open"), "fa fa-ext fa-diff-renamed", OpenRequestsPage.class) {

				@Override
				public boolean isActive(Page currentPage) {
					if (currentPage instanceof RequestDetailPage) {
						RequestDetailPage detailPage = (RequestDetailPage) currentPage;
						if (detailPage.getPullRequest().isOpen())
							return true;
					}
					return super.isActive(currentPage);
				}
			
		});
		requestsTabs.add(new RepositoryTab(Model.of("Closed"), "fa fa-circle-o", ClosedRequestsPage.class) {
			
				@Override
				public boolean isActive(Page currentPage) {
					if (currentPage instanceof RequestDetailPage) {
						RequestDetailPage detailPage = (RequestDetailPage) currentPage;
						if (!detailPage.getPullRequest().isOpen())
							return true;
					}
					return super.isActive(currentPage);
				}
			
		});
		requestsTabs.add(new RepositoryTab(Model.of("Create"), "fa fa-plus", NewRequestPage.class));		
		
		add(new Tabbable("requestsTabs", requestsTabs));		
		
		List<Tab> adminTabs = Lists.newArrayList();
		adminTabs.add(new RepositoryTab(Model.of("General Settings"), "fa fa-gear", GeneralSettingPage.class));
		adminTabs.add(new RepositoryTab(Model.of("Permissions"), "fa fa-lock", PermissionSettingPage.class));
		adminTabs.add(new RepositoryTab(Model.of("Gate Keepers"), "fa fa-ext fa-gatekeeper", GateKeeperPage.class));
		adminTabs.add(new RepositoryTab(Model.of("Integration Policies"), "fa fa-puzzle-piece", IntegrationPolicyPage.class));

		add(new Tabbable("adminTabs", adminTabs) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository())));
			}
			
		});
	}
	
	// TODO: here can be slow?
	private boolean isPubliclyAccessible() {
		Team anonymous = GitPlex.getInstance(TeamManager.class).getAnonymous(getAccount());
		for (Authorization each : getRepository().getAuthorizations()) {
			if (each.getTeam().isAnonymous()) {
				return each.getOperation().can(GeneralOperation.READ);
			}
		}
		
		if (anonymous.getAuthorizedOperation().can(GeneralOperation.READ)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(
				ObjectPermission.ofRepositoryRead(repoModel.getObject()));
	}
	
	public Repository getRepository() {
		return repoModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		if (repoModel != null) 
			repoModel.detach();
		
		super.onDetach();
	}

	public @Nullable String getCurrentRevision() {
		return currentRevision;
	}

	public @Nullable String getCurrentPath() {
		return currentPath;
	}

}
