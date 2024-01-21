package io.onedev.server.web.editable.workingperiod;

import javax.validation.ValidationException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

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
		input = new TextField<>("input", Model.of(period));
		add(input);
		input.setLabel(Model.of(getDescriptor().getDisplayName()));
		
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		input.add(newPlaceholderModifier());
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

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
