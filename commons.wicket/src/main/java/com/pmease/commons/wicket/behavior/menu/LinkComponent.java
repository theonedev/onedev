package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class LinkComponent extends Panel {

	private final String label;
	
	public LinkComponent(String id, String label) {
		super(id);
		
		this.label = label;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Link<Void> link = new Link<Void>("link") {

			@Override
			public void onClick() {
				LinkComponent.this.onClick();
			}
			
		};
		link.add(new Label("label", label));
		add(link);
	}

	protected abstract void onClick();
}
