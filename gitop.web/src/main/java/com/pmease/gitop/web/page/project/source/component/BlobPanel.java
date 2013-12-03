package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Project;

@SuppressWarnings("serial")
public class BlobPanel extends AbstractSourcePagePanel {

	public BlobPanel(String id, IModel<Project> projectModel,
			IModel<String> revisionModel, IModel<List<String>> pathsModel) {
		super(id, projectModel, revisionModel, pathsModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
	
}
