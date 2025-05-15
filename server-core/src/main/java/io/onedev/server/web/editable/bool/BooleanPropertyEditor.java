package io.onedev.server.web.editable.bool;

import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

public class BooleanPropertyEditor extends PropertyEditor<Boolean> {

	private CheckBox input;
	
	public BooleanPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Boolean> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(input = new CheckBox("input", Model.of(getModelObject())));
		
		input.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));
	}

	@Override
	protected Boolean convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
