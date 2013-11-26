package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;

@SuppressWarnings("serial")
public class SourceTreePanel extends ProjectPanel {

	private final IModel<List<String>> pathModel;
	private final IModel<String> revisionModel;
	
	public SourceTreePanel(String id, 
			IModel<Project> project,
			IModel<String> revisionModel,
			IModel<List<String>> pathModel) {
		
		super(id, project);
		
		this.revisionModel = revisionModel;
		this.pathModel = pathModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
	}
	
	@Override
	protected void onDetach() {
		if (pathModel != null) {
			pathModel.detach();
		}
		
		if (revisionModel != null) {
			revisionModel.detach();
		}
		
		super.onDetach();
	}
}
