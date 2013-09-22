package com.pmease.commons.wicket.editable.password;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class PasswordPropertyEditContext extends PropertyEditContext {

	public PasswordPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		PasswordTextField editor = new PasswordTextField((String) renderParam, new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return (String) getPropertyValue();
			}

			@Override
			public void setObject(String object) {
				setPropertyValue(object);
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("input");
				tag.put("type", "password");
				tag.put("class", "form-control");
				
				super.onComponentTag(tag);
			}
			
		};
		editor.setRequired(false);
		editor.setResetPassword(false);
		return editor;
	}

	@Override
	public Object renderForView(Object renderParam) {
		if (getPropertyValue() != null)
			return new Label((String) renderParam, "******");
		else
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		return null;
	}

}
