package com.pmease.commons.wicket.editable.password;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class PasswordPropertyEditor extends Panel {

	private final PasswordPropertyEditContext editContext;
	
	public PasswordPropertyEditor(String id, PasswordPropertyEditContext editContext) {
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

		fragment.add(new PasswordTextField("input", new IModel<String>() {

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
			
		}).setResetPassword(false).setRequired(false));
		
		add(fragment);
	}

}
