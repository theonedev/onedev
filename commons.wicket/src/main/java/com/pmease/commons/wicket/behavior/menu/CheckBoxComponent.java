package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;

@SuppressWarnings("serial")
class CheckBoxComponent extends Panel {

	private final CheckBoxItem checkBoxItem;
	
	public CheckBoxComponent(String id, CheckBoxItem checkBoxItem) {
		super(id);
		
		this.checkBoxItem = checkBoxItem;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxCheckBox("checkbox", checkBoxItem.getCheckModel()) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				findParent(DropdownPanel.class).hide(target);
				target.add(findParent(ContentPanel.class));
				checkBoxItem.onUpdate(target);
			}
			
		});
		
		add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return checkBoxItem.getLabel();
			}
			
		}));
	}

}
