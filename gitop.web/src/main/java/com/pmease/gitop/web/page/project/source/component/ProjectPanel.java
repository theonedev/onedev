package com.pmease.gitop.web.page.project.source.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;

@SuppressWarnings("serial")
public class ProjectPanel extends Panel {

	public ProjectPanel(String id, IModel<Repository> model) {
		super(id, model);
	}

	protected Repository getProject() {
		return (Repository) getDefaultModelObject();
	}
}
