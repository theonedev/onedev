package com.pmease.gitplex.web.page.repository;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
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
import com.google.common.base.Splitter;
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
import com.pmease.gitplex.web.component.repository.RepositoryAware;
import com.pmease.gitplex.web.model.RepositoryModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.home.AccountHomePage;
import com.pmease.gitplex.web.page.repository.admin.RepoOptionsPage;

@SuppressWarnings("serial")
public abstract class RepositoryPage extends AccountPage implements RepositoryAware {

	public static final String PARAM_REPO = "repo";

	public static final String PARAM_REVISION = "revision";
	
	public static final String PARAM_PATH = "path";

	protected IModel<Repository> repositoryModel;
	
	private final String revision;
	
	private final String objPath;
	
	public static PageParameters paramsOf(Repository repository) {
		return paramsOf(repository, null);
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision) {
		return paramsOf(repository, revision, null);
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		PageParameters params = paramsOf(repository.getUser());
		params.set(PARAM_REPO, repository.getName());
		if (StringUtils.isNotBlank(revision))
			params.set(PARAM_REVISION, revision);
		if (StringUtils.isNotBlank(path))
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
		
		repositoryModel = new RepositoryModel(repository);

		String revision = params.get(PARAM_REVISION).toString();
		
		if (StringUtils.isBlank(revision)) 
			revision = null;
		
		this.revision = revision;
		
		String objPath = params.get(PARAM_PATH).toString();
		if (StringUtils.isBlank(objPath))
			objPath = null;
		
		this.objPath = objPath;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Repository repository = getRepository();
		if (repository.getForkedFrom() != null)
			add(new Icon("repoIcon", "icon-repo-forked"));
		else
			add(new Icon("repoIcon", "icon-repo"));
		
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
				RepoOptionsPage.class, paramsOf(getRepository())) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository())));
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
				ObjectPermission.ofRepositoryRead(repositoryModel.getObject()));
	}
	
	public Repository getRepository() {
		return repositoryModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		if (repositoryModel != null) 
			repositoryModel.detach();
		
		super.onDetach();
	}

	@Override
	public String getCurrentRevision() {
		if (revision != null)
			return revision;
		else
			return getRepository().getDefaultBranch();
	}

	@Override
	public String getCurrentPath() {
		return objPath;
	}

	@Override
	public List<String> getCurrentPathSegments() {
		if (objPath != null)
			return Splitter.on("/").splitToList(objPath);
		else
			return Collections.emptyList();
	}

}
