package com.pmease.gitplex.web.page.repository.info.code.component;

import com.pmease.gitplex.web.page.repository.info.RepoInfoPanel;

@SuppressWarnings("serial")
public class SourceBreadcrumbPanel extends RepoInfoPanel {

	public SourceBreadcrumbPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revselector"));
		add(new PathsBreadcrumb("paths"));
	}
}
