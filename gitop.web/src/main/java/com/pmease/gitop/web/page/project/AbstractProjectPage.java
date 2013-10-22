package com.pmease.gitop.web.page.project;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.web.model.ProjectModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.account.AbstractAccountPage;
import com.pmease.gitop.web.page.project.settings.ProjectOptionsPage;

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
		
		Project project = Gitop.getInstance(ProjectManager.class).find(
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
		
		AbstractLink adminLink = new BookmarkablePageLink<Void>("adminlink", 
				ProjectOptionsPage.class, PageSpec.forProject(getProject())) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				this.setVisibilityAllowed(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(getProject())));
			}
		};
		
		add(adminLink);
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
