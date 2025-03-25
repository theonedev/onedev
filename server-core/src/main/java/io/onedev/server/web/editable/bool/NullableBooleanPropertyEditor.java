package io.onedev.server.web.editable.bool;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.TextUtils;

public class NullableBooleanPropertyEditor extends PropertyEditor<Boolean> {

	private StringSingleChoice input;
	
	public NullableBooleanPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Boolean> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String selection;
		if (getModelObject() != null)
			selection = TextUtils.getDisplayValue(getModelObject());
		else
			selection = null;

		input = new StringSingleChoice("input", Model.of(selection), new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return List.of(TextUtils.getDisplayValue(true), TextUtils.getDisplayValue(false));
			}

		}, false) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
			}

		};
		input.setLabel(Model.of(getDescriptor().getDisplayName()));

		input.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}

		});

		add(input);
	}

	@Override
	protected Boolean convertInputToValue() throws ConversionException {
		String stringValue = input.getConvertedInput();
		if (TextUtils.getDisplayValue(true).equals(stringValue))
			return true;
		else if (TextUtils.getDisplayValue(false).equals(stringValue))
			return false;
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
