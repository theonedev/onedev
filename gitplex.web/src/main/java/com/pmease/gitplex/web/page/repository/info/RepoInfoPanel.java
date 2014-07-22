package com.pmease.gitplex.web.page.repository.info;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class RepoInfoPanel extends Panel {

	public RepoInfoPanel(String id) {
		super(id);
	}

	public RepoInfoPanel(String id, IModel<?> model) {
		super(id, model);
	}
	
	protected RepositoryInfoPage getRepoInfoPage() {
		return (RepositoryInfoPage) getPage();
	}
	
	protected Repository getRepository() {
		return getRepoInfoPage().getRepository();
	}
	
	protected String getRevision() {
		return getRepoInfoPage().getRevision();
	}
	
	protected String getObjPath() {
		return getRepoInfoPage().getObjPath();
	}
	
	protected List<String> getObjPathSegments() {
		return getRepoInfoPage().getObjPathSegments();
	}

}
