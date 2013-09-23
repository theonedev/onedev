package com.pmease.commons.wicket.editable.string;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.EditableResourceBehavior;

@SuppressWarnings("serial")
public class StringPropertyEditContext extends PropertyEditContext {

	public StringPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		return new TextField<String>((String) renderParam, new IModel<String>() {

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
			
		}.add(new EditableResourceBehavior());
	}

	@Override
	public Object renderForView(Object renderParam) {
		if (getPropertyValue() != null)
			return new Label((String) renderParam, (String) getPropertyValue());
		else
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		return null;
	}

}
