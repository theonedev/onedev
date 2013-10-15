package com.pmease.gitop.web.page.project;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.GeneralException;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.permission.ObjectPermission;
import com.pmease.gitop.web.page.AbstractLayoutPage;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class ProjectHomePage extends AbstractLayoutPage {

	private final IModel<Project> projectModel;
	
	@Override
	protected String getPageTitle() {
		return "Project Home";
	}

	public ProjectHomePage(PageParameters params) {
		String userName = params.get(PageSpec.USER).toString();
		String projectName = params.get(PageSpec.PROJECT).toString();
		
		if (projectName.endsWith(".git"))
			projectName = projectName.substring(0, projectName.length() - ".git".length());
		
		Project project = Gitop.getInstance(ProjectManager.class).find(userName, projectName);
		
		if (project == null)
			throw new GeneralException("Can not find project %s under account %s.", projectName, userName);
		
		final Long projectId = project.getId();
		
		projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				return Gitop.getInstance(ProjectManager.class).load(projectId);
			}
			
		};
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(getProject()));
	}
	
	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new Label("accountName", getProject().getOwner().getName()));
		add(new Label("projectName", getProject().getName()));
	}
	
	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	public void detachModels() {
		if (projectModel != null)
			projectModel.detach();
		super.detachModels();
	}
}
