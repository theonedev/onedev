package com.pmease.gitop.web.page.repository.source.component;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;

@SuppressWarnings("serial")
public class RepositoryPanel extends Panel {

	public RepositoryPanel(String id, IModel<Repository> model) {
		super(id, model);
	}

	protected Repository getRepository() {
		return (Repository) getDefaultModelObject();
	}
}
