package com.pmease.commons.web.behavior.popover;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
abstract class ContentPanel extends Panel {

	private IModel<String> titleModel;
	
	public ContentPanel(String id, IModel<String> titleModel) {
		super(id);
		this.titleModel = titleModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new Label("title", titleModel));
		add(newBody("body"));
	}
	
	protected abstract Component newBody(String id);
}
