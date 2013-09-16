package com.pmease.commons.wicket.editable.nuemric;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.RenderableEditContext;

@SuppressWarnings("serial")
public class NumericPropertyEditContext extends PropertyEditContext implements RenderableEditContext {

	private final IModel<String> inputModel;
	
	public NumericPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
		Integer propertyValue = (Integer) getPropertyValue();
		if (propertyValue != null)
			inputModel = new Model<String>(propertyValue.toString());
		else
			inputModel = new Model<String>("");
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new TextField<String>(componentId, inputModel) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("input");
				tag.put("type", "text");
				super.onComponentTag(tag);
			}

		};
	}

	@Override
	public Component renderForView(String componentId) {
		Object propertyValue = getPropertyValue();
		if (propertyValue != null)
			return new Label(componentId, propertyValue.toString());
		else
			return new Label(componentId);
	}

	@Override
	protected void doValidation() {
		String input = inputModel.getObject();
		Integer convertedInput;
		try {
			if (input != null) {
				convertedInput = Integer.valueOf(input);
			} else {
				convertedInput = null;
			}
			setPropertyValue(convertedInput);
		} catch (NumberFormatException e) {
			error("Expects a number here.");
		} catch (IllegalArgumentException e) {
			error("Please specify a number here.");
		}
		
		super.doValidation();
	}

}
