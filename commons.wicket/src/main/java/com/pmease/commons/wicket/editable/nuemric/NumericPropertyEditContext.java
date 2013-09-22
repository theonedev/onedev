package com.pmease.commons.wicket.editable.nuemric;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;

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
	public Object renderForEdit(Object renderParam) {
		return new NumericPropertyEditor((String) renderParam, this);
	}

	@Override
	public Object renderForView(Object renderParam) {
		Object propertyValue = getPropertyValue();
		if (propertyValue != null)
			return new Label((String) renderParam, propertyValue.toString());
		else
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

	@Override
	protected void doValidation() {
		String input = inputModel.getObject();

		Serializable convertedInput;
		
		try {
			if (input != null) {
				if (getPropertyGetter().getReturnType() == int.class || getPropertyGetter().getReturnType() == Integer.class)
					convertedInput = Integer.valueOf(input);
				else
					convertedInput = Long.valueOf(input);
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

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		return null;
	}

}
