package com.pmease.commons.wicket.editable.bool;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class BooleanPropertyEditContext extends PropertyEditContext {

	public BooleanPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(Object renderParam) {
		return new CheckBox((String)renderParam, new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return (Boolean) getPropertyValue();
			}

			@Override
			public void setObject(Boolean object) {
				setPropertyValue(object);
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("input");
				tag.put("type", "checkbox");
				super.onComponentTag(tag);
			}
			
		};
	}

	@Override
	public Component renderForView(Object renderParam) {
		return new Label((String) renderParam, getPropertyValue().toString());
	}

}
