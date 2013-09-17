package com.pmease.commons.wicket.editable.string;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class StringPropertyEditor extends Panel {

	private final StringPropertyEditContext editContext;
	
	public StringPropertyEditor(String id, StringPropertyEditContext editContext) {
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

		fragment.add(new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return (String) editContext.getPropertyValue();
			}

			@Override
			public void setObject(String object) {
				editContext.setPropertyValue(object);
			}
			
		}));
		
		add(fragment);
	}

}
