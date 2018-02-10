package com.turbodev.server.web.editable.date;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.server.util.editable.EditableUtils;
import com.turbodev.server.util.editable.annotation.OmitName;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextFieldConfig;

@SuppressWarnings("serial")
public class DatePropertyEditor extends PropertyEditor<Date> {

	private FormComponent<Date> input;
	
	public DatePropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Date> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DateTextFieldConfig config = new DateTextFieldConfig();
		config.autoClose(true);
		config.clearButton(true);
		config.withFormat(DateEditSupport.DATE_INPUT_FORMAT);
		input = new DateTextField("input", Model.of(getModelObject()), config);
		input.setType(getPropertyDescriptor().getPropertyClass());
		Method propertyGetter = getPropertyDescriptor().getPropertyGetter();
		if (propertyGetter.getAnnotation(OmitName.class) != null)
			input.add(AttributeModifier.replace("placeholder", EditableUtils.getName(propertyGetter)));
		
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
	protected Date convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
