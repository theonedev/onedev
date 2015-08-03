package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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
				if (tickItem.isChecked())
					return "fa fa-check checked check";
				else
					return "unchecked check";
			}
			
		})));
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				tickItem.onClick(target);
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
