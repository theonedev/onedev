package com.pmease.commons.wicket.editable.nuemric;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

@SuppressWarnings("serial")
public class NumericPropertyEditor extends Panel {

	private final NumericPropertyEditContext editContext;
	
	public NumericPropertyEditor(String id, NumericPropertyEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Fragment fragment;
		if (editContext.isPropertyRequired()) {
			fragment = new Fragment("content", "required", this);
		} else {
			fragment = new Fragment("content", "notRequired", this);
		}
		fragment.add(new TextField<String>("input", editContext.getInputModel()));
		add(fragment);
	}

}
