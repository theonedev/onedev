package io.onedev.server.web.editable.bool;

import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.TextUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
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

		input = new StringSingleChoice("input", Model.of(selection), new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices = new LinkedHashMap<>();
				choices.put(TextUtils.getDisplayValue(true), TextUtils.getDisplayValue(true));
				choices.put(TextUtils.getDisplayValue(false), TextUtils.getDisplayValue(false));
				return choices;
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
