package com.pmease.gitop.web.page.repository;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.model.permission.operation.GeneralOperation;
import com.pmease.gitop.web.SessionData;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.model.RepositoryModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.AbstractAccountPage;
import com.pmease.gitop.web.page.repository.source.RepositoryHomePage;

@SuppressWarnings("serial")
public abstract class RepositoryBasePage extends AbstractAccountPage {

	protected IModel<Repository> repositoryModel;
	
	public RepositoryBasePage(PageParameters params) {
		super(params);
		
		String repositoryName = params.get(PageSpec.REPO).toString();
		Preconditions.checkNotNull(repositoryName);
		
		if (repositoryName.endsWith(Constants.DOT_GIT_EXT))
			repositoryName = repositoryName.substring(0, 
					repositoryName.length() - Constants.DOT_GIT_EXT.length());
		
		Repository repository = Gitop.getInstance(RepositoryManager.class).findBy(
				getAccount(), repositoryName);
		
		if (repository == null) {
			throw new EntityNotFoundException("Unable to find repository " 
						+ getAccount() + "/" + repositoryName);
		}
		
		repositoryModel = new RepositoryModel(repository);
		if (!Objects.equal(SessionData.get().getRepositoryId(), repository.getId())) {
			// displayed repository changed
			SessionData.get().onRepositoryChanged();
		}
		
		SessionData.get().setRepositoryId(repository.getId());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Repository repository = getRepository();
		if (repository.getForkedFrom() != null)
			add(new Icon("repoIcon", "icon-repo-forked"));
		else
			add(new Icon("repoIcon", "icon-repo"));
		
		AbstractLink userlink = PageSpec.newUserHomeLink("userlink", repository.getOwner());
		add(userlink);
		userlink.add(new Label("name", Model.of(repository.getOwner().getName())));
		
		AbstractLink repositoryLink = PageSpec.newRepositoryHomeLink("repositorylink", repository);
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
		
		add(new Link<Void>("fork") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				UserManager userManager = Gitop.getInstance(UserManager.class);
				User currentUser = userManager.getCurrent();
				setVisible(getRepository().isForkable() 
						&& currentUser != null 
						&& !getRepository().getOwner().equals(currentUser));
			}

			@Override
			public void onClick() {
				User currentUser = Gitop.getInstance(UserManager.class).getCurrent();
				Repository forked = Gitop.getInstance(RepositoryManager.class).fork(getRepository(), currentUser);
				setResponsePage(RepositoryHomePage.class, PageSpec.forRepository(forked));
			}
			
		});
	}
	
	// TODO: here can be slow?
	private boolean isPubliclyAccessible() {
		Team anonymous = Gitop.getInstance(TeamManager.class).getAnonymous(getAccount());
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
		if (repositoryModel != null) {
			repositoryModel.detach();
		}
		
		super.onDetach();
	}
}
