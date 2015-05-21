package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
class CheckComponent extends Panel {

	private final CheckItem menuItem;
	
	public CheckComponent(String id, CheckItem menuItem) {
		super(id);
		
		this.menuItem = menuItem;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxCheckBox("checkbox", menuItem.getCheckModel()) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				menuItem.onUpdate(target);
			}
			
		});
		
		add(new Label("label", menuItem.getLabel()));
	}

}
