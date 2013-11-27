package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;

@SuppressWarnings("serial")
public class SourceTreePanel extends AbstractSourcePagePanel {
	public SourceTreePanel(String id, 
			IModel<Project> project,
			IModel<String> revisionModel,
			IModel<List<String>> pathsModel) {
		super(id, project, revisionModel, pathsModel);
	}
}
