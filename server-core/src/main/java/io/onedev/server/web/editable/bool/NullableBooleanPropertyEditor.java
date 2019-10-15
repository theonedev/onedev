package io.onedev.server.web.editable.bool;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.collect.Lists;

import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

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
		input.setLabel(Model.of(getDescriptor().getDisplayName()));
		
		input.setNullValid(true);
		
		add(input);

		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
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
