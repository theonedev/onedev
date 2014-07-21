package com.pmease.gitplex.web.common.wicket.component.link;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class LinkPanel extends Panel {

	private final IModel<String> labelModel;
	
	public LinkPanel(String id, IModel<?> model, IModel<String> labelModel) {
		super(id, model);
		this.labelModel = labelModel;
	}

	public LinkPanel(String id, IModel<String> labelModel) {
		this(id, null, labelModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AbstractLink link = createLink("link");
		add(link);
		link.add(new Label("text", labelModel.getObject()));
	}

	@Override
	public void onDetach() {
		if (labelModel != null) {
			labelModel.detach();
		}
		
		super.onDetach();
	}
	
	protected abstract AbstractLink createLink(String id);
}
