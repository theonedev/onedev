package com.pmease.commons.wicket.editable.password;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class ConfirmativePasswordPropertyEditor extends PropertyEditor<String> {
	
	private PasswordTextField input;
	
	private PasswordTextField inputAgain;
	
	public ConfirmativePasswordPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new PasswordTextField("input", Model.of(getModelObject()));
		input.setResetPassword(true);
		input.setRequired(false);
		add(input);
		
		inputAgain = new PasswordTextField("inputAgain", Model.of(getModelObject()));
		inputAgain.setResetPassword(true);
		inputAgain.setRequired(false);
		add(inputAgain);
		
		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasError(true))
					return " has-error";
				else
					return "";
			}
			
		}));
		
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		if (input.getConvertedInput() != null) {
			if (inputAgain.getConvertedInput() == null)
				throw new ConversionException("Please confirm the password.");
			else if (!input.getConvertedInput().equals(inputAgain.getConvertedInput()))
				throw new ConversionException("Password and its confirmation should be identical.");
			else
				return input.getConvertedInput();
		} else if (inputAgain.getConvertedInput() != null) {
			throw new ConversionException("Password and its confirmation should be identical.");
		} else {
			return input.getConvertedInput();
		}
	}

}
