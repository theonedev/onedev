package com.pmease.gitop.web.common.wicket.component.link;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class LinkPanel extends Panel {

	public LinkPanel(String id, IModel<String> label) {
		super(id, label);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AbstractLink link = createLink("link");
		add(link);
		link.add(new Label("text", getDefaultModel()));
	}
	
	protected abstract AbstractLink createLink(String id);
}
