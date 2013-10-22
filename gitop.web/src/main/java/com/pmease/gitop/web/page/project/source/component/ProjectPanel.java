package com.pmease.gitop.web.page.project.source.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;

@SuppressWarnings("serial")
public class ProjectPanel extends Panel {

	public ProjectPanel(String id, IModel<Project> model) {
		super(id, model);
	}

	protected Project getProject() {
		return (Project) getDefaultModelObject();
	}
}
