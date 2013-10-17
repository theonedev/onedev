package com.pmease.gitop.web.page.project;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.ObjectPermission;
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
		
		User user = accountModel.getObject();
		Project project = Gitop.getInstance(ProjectManager.class).find(
				user, projectName);
		
		if (project == null) {
			throw new EntityNotFoundException("Unable to find project " 
						+ user.getName() + " / " + projectName);
		}
		
		projectModel = new ProjectModel(project);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(
				ObjectPermission.ofProjectRead(projectModel.getObject()));
	}
}
