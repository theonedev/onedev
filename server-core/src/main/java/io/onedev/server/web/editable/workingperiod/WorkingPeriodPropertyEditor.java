package io.onedev.server.web.editable.workingperiod;

import java.lang.reflect.Method;

import javax.validation.ValidationException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;

@SuppressWarnings("serial")
public class WorkingPeriodPropertyEditor extends PropertyEditor<Integer> {

	private TextField<String> input;
	
	public WorkingPeriodPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Integer> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String period;
		if (getModelObject() != null)
			period = DateUtils.formatWorkingPeriod(getModelObject()).toString();
		else
			period = null;
		input = new TextField<String>("input", Model.of(period));
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
	protected Integer convertInputToValue() throws ConversionException {
		if (StringUtils.isNotBlank(input.getConvertedInput())) {
			try {
				return DateUtils.parseWorkingPeriod(input.getConvertedInput());
			} catch (ValidationException e) {
				throw new ConversionException(e.getMessage());
			}
		} else {
			return null;
		}
	}

}
