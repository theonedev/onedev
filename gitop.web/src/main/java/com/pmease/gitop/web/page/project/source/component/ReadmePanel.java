package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Project;

@SuppressWarnings("serial")
public class ReadmePanel extends AbstractSourcePagePanel {

	public ReadmePanel(String id, 
			IModel<Project> model,
			IModel<String> revisionModel,
			IModel<List<String>> pathsModel) {
		super(id, model, revisionModel, pathsModel);
	}
}
