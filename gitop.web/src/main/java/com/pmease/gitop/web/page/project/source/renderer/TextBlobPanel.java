package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.GitBlob;

@SuppressWarnings("serial")
public class TextBlobPanel extends Panel {

	public TextBlobPanel(String id, IModel<GitBlob> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("code", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getBlob().getStringContent();
			}
			
		}));
		
	}
	
	private GitBlob getBlob() {
		return (GitBlob) getDefaultModelObject();
	}
}
