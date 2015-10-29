package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
abstract class AjaxLinkComponent extends Panel {

	private final String label;
	
	public AjaxLinkComponent(String id, String label) {
		super(id);
		
		this.label = label;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		AjaxLink<Void> link = new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				AjaxLinkComponent.this.onClick(target);
			}
			
		};
		link.add(new Label("label", label));
		add(link);
	}

	protected abstract void onClick(AjaxRequestTarget target);
}
