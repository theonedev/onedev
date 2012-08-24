package com.pmease.commons.wicket.behavior.popover;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;

@SuppressWarnings("serial")
public abstract class PopoverPanel extends DropdownPanel {
	
	private IModel<String> titleModel;
	
	public PopoverPanel(String id, IModel<String> titleModel) {
		super(id);
		this.titleModel = titleModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeModifier.append("class", "popover"));
	}

	@Override
	protected Component newContent(String id) {
		return new ContentPanel(id, titleModel) {

			@Override
			protected Component newBody(String id) {
				return PopoverPanel.this.newBody(id);
			}
			
		};
	}
	
	protected abstract Component newBody(String id);
}
