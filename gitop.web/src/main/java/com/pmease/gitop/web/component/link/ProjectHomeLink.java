package com.pmease.gitop.web.component.link;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.component.link.LinkPanel;
import com.pmease.gitop.web.page.PageSpec;

public class ProjectHomeLink extends LinkPanel {
	private static final long serialVersionUID = 1L;

	private final IModel<Project> repoModel;
	
	@SuppressWarnings("serial")
	public ProjectHomeLink(String id, final IModel<Project> repoModel) {
		super(id, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Project project = repoModel.getObject();
				return project.getOwner().getName() + "/" + project.getName();
			}
			
		});
		
		this.repoModel = repoModel;
	}

	@Override
	protected AbstractLink createLink(String id) {
		return PageSpec.newProjectHomeLink(id, repoModel.getObject());
	}

	@Override
	public void onDetach() {
		if (repoModel != null) {
			repoModel.detach();
		}
		
		super.onDetach();
	}
}
