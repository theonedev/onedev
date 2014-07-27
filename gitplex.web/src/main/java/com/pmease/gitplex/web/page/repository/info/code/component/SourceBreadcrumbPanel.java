package com.pmease.gitplex.web.page.repository.info.code.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class SourceBreadcrumbPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private final String path;
	
	public SourceBreadcrumbPanel(String id, IModel<Repository> repoModel, String revision, String path) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
		this.path = path;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revselector", repoModel, revision, path));
		add(new PathsBreadcrumb("paths", repoModel, revision, path));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
