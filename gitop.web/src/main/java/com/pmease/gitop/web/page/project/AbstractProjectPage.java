package com.pmease.gitop.web.page.project;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.model.permission.operation.GeneralOperation;
import com.pmease.gitop.web.model.ProjectModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.AbstractAccountPage;

@SuppressWarnings("serial")
public abstract class AbstractProjectPage extends AbstractAccountPage {

	protected IModel<Project> projectModel;
	
	public AbstractProjectPage(PageParameters params) {
		super(params);
		
		String projectName = params.get(PageSpec.PROJECT).toString();
		Preconditions.checkNotNull(projectName);
		
		if (projectName.endsWith(Constants.DOT_GIT_EXT))
			projectName = projectName.substring(0, 
					projectName.length() - Constants.DOT_GIT_EXT.length());
		
		Project project = Gitop.getInstance(ProjectManager.class).findBy(
				getAccount(), projectName);
		
		if (project == null) {
			throw new EntityNotFoundException("Unable to find project " 
						+ getAccount() + "/" + projectName);
		}
		
		projectModel = new ProjectModel(project);
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		Project project = getProject();
		AbstractLink userlink = PageSpec.newUserHomeLink("userlink", project.getOwner());
		add(userlink);
		userlink.add(new Label("name", Model.of(project.getOwner().getName())));
		
		AbstractLink projectLink = PageSpec.newProjectHomeLink("projectlink", project);
		add(projectLink);
		projectLink.add(new Label("name", Model.of(project.getName())));
		
		Label publicLabel = new Label("public-label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return isPubliclyAccessible() ? "public" : "private";
			}
		}) {
			@Override
			public void onEvent(IEvent<?> event) {
				if (event.getPayload() instanceof ProjectPubliclyAccessibleChanged) {
					ProjectPubliclyAccessibleChanged e = (ProjectPubliclyAccessibleChanged) event.getPayload();
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
	}
	
	// TODO: here can be slow?
	private boolean isPubliclyAccessible() {
		Team anonymous = Gitop.getInstance(TeamManager.class).getAnonymous(getAccount());
		for (Authorization each : getProject().getAuthorizations()) {
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
				ObjectPermission.ofProjectRead(projectModel.getObject()));
	}
	
	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
		}
		
		super.onDetach();
	}
}
