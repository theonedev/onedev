package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.GitBlob;

@SuppressWarnings("serial")
public class GeneralBlobPanel extends Panel {

	public GeneralBlobPanel(String id, IModel<GitBlob> model) {
		super(id, model);
	}

}
