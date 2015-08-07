package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;

@SuppressWarnings("serial")
class CheckComponent extends Panel {

	private final CheckItem checkItem;
	
	public CheckComponent(String id, CheckItem checkItem) {
		super(id);

		this.checkItem = checkItem;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("check").add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (checkItem.isChecked())
					return "fa fa-check checked check";
				else
					return "unchecked check";
			}
			
		})));
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				checkItem.updateAjaxAttributes(attributes);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				findParent(DropdownPanel.class).hide(target);
				target.add(findParent(ContentPanel.class));
				checkItem.onClick(target);
			}
			
		};
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return checkItem.getLabel();
			}
			
		}));
		add(link);
	}
	
}
