package com.pmease.commons.wicket.editable.nuemric;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class NumericPropertyEditContext extends PropertyEditContext<RenderContext> {

	private final IModel<String> inputModel;
	
	public NumericPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
		Integer propertyValue = (Integer) getPropertyValue();
		if (propertyValue != null)
			inputModel = new Model<String>(propertyValue.toString());
		else
			inputModel = new Model<String>("");
	}
	
	public IModel<String> getInputModel() {
		return inputModel;
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new NumericPropertyEditor(renderContext.getComponentId(), this));
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		Object propertyValue = getPropertyValue();
		if (propertyValue != null)
			renderContext.getContainer().add(new Label(renderContext.getComponentId(), propertyValue.toString()));
		else
			renderContext.getContainer().add(new Label(renderContext.getComponentId()));
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

	@Override
	public Map<Serializable, EditContext<RenderContext>> getChildContexts() {
		return null;
	}

}
