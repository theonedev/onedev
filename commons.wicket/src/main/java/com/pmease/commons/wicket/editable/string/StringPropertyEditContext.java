package com.pmease.commons.wicket.editable.string;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class StringPropertyEditContext extends PropertyEditContext {

	public StringPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new TextField<String>(componentId, new IModel<String>() {

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
				tag.put("type", "text");
				tag.put("class", "form-control");
				
				super.onComponentTag(tag);
			}
			
		};
	}

	@Override
	public Component renderForView(String componentId) {
		if (getPropertyValue() != null)
			return new Label(componentId, (String) getPropertyValue());
		else
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

}
