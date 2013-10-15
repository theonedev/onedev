package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.common.component.link.LinkPanel;
import com.pmease.gitop.web.page.PageSpec;

public class ProjectHomeLink extends LinkPanel {
	private static final long serialVersionUID = 1L;

	private final IModel<Project> projectModel;
	
	@SuppressWarnings("serial")
	public ProjectHomeLink(String id, final IModel<Project> projectModel) {
		super(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Project project = projectModel.getObject();
				return project.getOwner().getName() + " / " + project.getName();
			}
			
		});
		
		this.projectModel = projectModel;
	}

	@Override
	protected AbstractLink createLink(String id) {
		return PageSpec.newProjectHomeLink(id, projectModel.getObject());
	}

	@Override
	public void onDetach() {
		if (projectModel != null) {
			projectModel.detach();
		}
		
		super.onDetach();
	}
}
