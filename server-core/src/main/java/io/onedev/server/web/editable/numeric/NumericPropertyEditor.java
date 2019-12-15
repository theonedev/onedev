package io.onedev.server.web.editable.numeric;

import java.lang.reflect.Method;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class NumericPropertyEditor extends PropertyEditor<Number> {

	private TextField<Number> input;
	
	public NumericPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Number> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new TextField<Number>("input", Model.of(getModelObject()));
		input.setType(getDescriptor().getPropertyClass());
		add(input);
		input.setLabel(Model.of(getDescriptor().getDisplayName()));
		
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		
		Method getter = getDescriptor().getPropertyGetter();
		if (getter.getAnnotation(OmitName.class) != null) {
			input.add(AttributeModifier.replace("placeholder", EditableUtils.getDisplayName(getter)));
		} else {
			NameOfEmptyValue nameOfEmptyValue = getter.getAnnotation(NameOfEmptyValue.class);
			if (nameOfEmptyValue != null)
				input.add(AttributeModifier.replace("placeholder", nameOfEmptyValue.value()));
		}
	}

	@Override
	protected Number convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
