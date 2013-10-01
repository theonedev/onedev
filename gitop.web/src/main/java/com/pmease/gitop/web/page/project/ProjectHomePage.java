package com.pmease.gitop.web.page.project;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.GeneralException;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class ProjectHomePage extends AbstractLayoutPage {

	private final IModel<Project> projectModel;
	
	@Override
	protected String getPageTitle() {
		return "Project Home";
	}

	public ProjectHomePage(PageParameters params) {
		String userName = params.get("user").toString();
		String projectName = params.get("project").toString();
		
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
		
		add(new Link<Void>("link") {

			@Override
			public void onClick() {
				
			}
			
		});
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("accountName", getProject().getOwner().getName()));
		add(new Label("projectName", getProject().getName()));
	}

	public Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	public void detachModels() {
		projectModel.detach();
		super.detachModels();
	}

	public static PageParameters paramsOf(Project project) {
		PageParameters params = new PageParameters();
		params.set("user", project.getOwner().getName());
		params.set("project", project.getName());
		
		return params;
	}

}
