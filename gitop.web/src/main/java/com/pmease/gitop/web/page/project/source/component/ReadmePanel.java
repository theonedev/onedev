package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;

@SuppressWarnings("serial")
public class ReadmePanel extends ProjectPanel {

	private final IModel<List<String>> pathModel;
	
	public ReadmePanel(String id, IModel<Project> model,
			IModel<List<String>> pathModel) {
		super(id, model);
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
