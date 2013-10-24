package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;

@SuppressWarnings("serial")
public class SourceTreePanel extends ProjectPanel {

	private final IModel<List<String>> pathModel;
	
	public SourceTreePanel(String id, IModel<Project> project, 
			IModel<List<String>> pathModel) {
		
		super(id, project);
		this.pathModel = pathModel;
	}

	@Override
	protected void onDetach() {
		if (pathModel != null) {
			pathModel.detach();
		}
		
		super.onDetach();
	}
}
