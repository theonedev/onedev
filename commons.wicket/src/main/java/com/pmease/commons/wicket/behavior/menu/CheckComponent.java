package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

@SuppressWarnings("serial")
class CheckComponent extends Panel {

	private final CheckItem tickItem;
	
	public CheckComponent(String id, CheckItem tickItem) {
		super(id);

		this.tickItem = tickItem;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("check").add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (tickItem.isTicked())
					return "fa fa-check checked check";
				else
					return "unchecked check";
			}
			
		})));
		
		Link<Void> link = new Link<Void>("link") {

			@Override
			public void onClick() {
				tickItem.onClick();
			}
			
		};
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return tickItem.getLabel();
			}
			
		}));
		add(link);
	}
	
}
