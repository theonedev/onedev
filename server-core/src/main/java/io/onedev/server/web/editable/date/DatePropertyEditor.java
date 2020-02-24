package io.onedev.server.web.editable.date;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextFieldConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.DateTextFieldConfig.TodayButton;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.OmitName;

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
		config.autoClose(true).withFormat(DateEditSupport.DATE_INPUT_FORMAT)
				.highlightToday(true).showTodayButton(TodayButton.TRUE);
		input = new DateTextField("input", Model.of(getModelObject()), config);
		input.setType(getDescriptor().getPropertyClass());
		Method propertyGetter = getDescriptor().getPropertyGetter();
		if (propertyGetter.getAnnotation(OmitName.class) != null)
			input.add(AttributeModifier.replace("placeholder", EditableUtils.getDisplayName(propertyGetter)));

		input.setLabel(Model.of(getDescriptor().getDisplayName()));
		add(input);
		
		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
	}

	@Override
	protected Date convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
