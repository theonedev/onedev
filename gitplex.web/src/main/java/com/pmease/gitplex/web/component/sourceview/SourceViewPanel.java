package com.pmease.gitplex.web.component.sourceview;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class SourceViewPanel extends Panel {

	public SourceViewPanel(String id, IModel<Repository> repoModel, String blobId) {
		super(id);
	}

}
