package com.pmease.gitplex.web.page.repository.source.component;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class SourceBreadcrumbPanel extends AbstractSourcePagePanel {

	public SourceBreadcrumbPanel(String id, 
			IModel<Repository> repositoryModel, 
			IModel<String> revisionModel, 
			IModel<List<String>> pathsModel) {
		super(id, repositoryModel, revisionModel, pathsModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revselector", repoModel, revisionModel, pathsModel));
		add(new PathsBreadcrumb("paths", repoModel, revisionModel, pathsModel));
	}
}
