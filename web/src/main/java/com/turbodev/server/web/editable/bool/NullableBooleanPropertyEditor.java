package com.turbodev.server.web.editable.bool;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.collect.Lists;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class NullableBooleanPropertyEditor extends PropertyEditor<Boolean> {

	private DropDownChoice<String> input;
	
	public NullableBooleanPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Boolean> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String stringValue;
		if (getModelObject() != null) {
			if (getModelObject())
				stringValue = "yes";
			else
				stringValue = "no";
		} else {
			stringValue = null;
		}
		input = new DropDownChoice<String>("input", Model.of(stringValue), Lists.newArrayList("yes", "no"));
		
		input.setNullValid(true);
		
		add(input);

		add(new AttributeAppender("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (hasErrors(true))
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
	protected Boolean convertInputToValue() throws ConversionException {
		String stringValue = input.getConvertedInput();
		if ("yes".equals(stringValue))
			return true;
		else if ("no".equals(stringValue))
			return false;
		else
			return null;
	}

}
