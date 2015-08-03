package com.pmease.commons.wicket.behavior.menu;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;

@SuppressWarnings("serial")
public abstract class MenuPanel extends DropdownPanel {

	public MenuPanel(String id, boolean lazyLoad) {
		super(id, lazyLoad);
	}
	
	public MenuPanel(String id) {
		this(id, false);
	}

	public MenuPanel(String id, IModel<?> model, boolean lazyLoad) {
		super(id, model, lazyLoad);
	}
	
	public MenuPanel(String id, IModel<?> model) {
		this(id, model, false);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeModifier.append("class", "menu"));
	}

	@Override
	protected Component newContent(String id) {
		return new ContentPanel(id) {

			@Override
			protected List<MenuItem> getMenuItems() {
				return MenuPanel.this.getMenuItems();
			}
			
		};
	}
	
	protected abstract List<MenuItem> getMenuItems();
}
