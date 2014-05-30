package com.pmease.commons.wicket.editable.nuemric;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class NumericPropertyEditContext extends PropertyEditContext {

	private final IModel<String> inputModel;
	
	public NumericPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
		Serializable propertyValue = getPropertyValue();
		if (propertyValue != null)
			inputModel = new Model<String>(propertyValue.toString());
		else
			inputModel = new Model<String>("");
	}
	
	public IModel<String> getInputModel() {
		return inputModel;
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new TextField<String>(componentId, getInputModel()) {

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
		Object propertyValue = getPropertyValue();
		if (propertyValue != null) {
			return new Label(componentId, propertyValue.toString());
		} else {
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

	@Override
	public void updateBean() {
		String input = inputModel.getObject();

		Serializable convertedInput;
		
		boolean isInt = getPropertyGetter().getReturnType() == int.class || getPropertyGetter().getReturnType() == Integer.class;
		
		try {
			if (input != null) {
				if (isInt)
					convertedInput = Integer.valueOf(input);
				else
					convertedInput = Long.valueOf(input);
			} else {
				convertedInput = null;
			}
			setPropertyValue(convertedInput);
		} catch (IllegalArgumentException e) {
			addValidationError("Expects a number here.");
			
			if (isInt)
				setPropertyValue(Integer.MIN_VALUE);
			else
				setPropertyValue(Long.MIN_VALUE);
		}
		
		super.updateBean();
	}

}
