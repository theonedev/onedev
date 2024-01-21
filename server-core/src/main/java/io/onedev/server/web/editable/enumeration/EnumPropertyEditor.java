package io.onedev.server.web.editable.enumeration;

import io.onedev.server.annotation.RadioChoice;
import io.onedev.server.web.component.stringchoice.StringRadioChoice;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.TextUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class EnumPropertyEditor extends PropertyEditor<Enum<?>> {

	private final Class<Enum> enumClass;
	
	private FormComponent<String> input;
	
	public EnumPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Enum<?>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
		enumClass = (Class<Enum>) propertyDescriptor.getPropertyGetter().getReturnType();		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var radioChoice = descriptor.getPropertyGetter().getAnnotation(RadioChoice.class) != null;
		Fragment fragment;
		if (radioChoice)
			fragment = new Fragment("content", "radioFrag", this);
		else 
			fragment = new Fragment("content", "choiceFrag", this);
		add(fragment);
		
		String selection;
        if (getModelObject() != null)
        	selection = getModelObject().name();
        else
        	selection = null;

		var choicesModel = new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices = new LinkedHashMap<>();
				for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext(); ) {
					Enum<?> value = (Enum<?>) it.next();
					choices.put(value.name(), TextUtils.getDisplayValue(value));
				}
				return choices;
			}

		};
		
		if (radioChoice) {
			fragment.add(input = new StringRadioChoice("input", Model.of(selection), choicesModel));			
		} else {
			fragment.add(input = new StringSingleChoice("input", Model.of(selection), choicesModel, false) {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					getSettings().configurePlaceholder(descriptor);
					getSettings().setAllowClear(!descriptor.isPropertyRequired());
				}

			});
		}
		
        input.setLabel(Model.of(getDescriptor().getDisplayName()));
		
		if (radioChoice) {
			input.add(new AjaxFormChoiceComponentUpdatingBehavior() {
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					onPropertyUpdating(target);					
				}
			});	
		} else {
			input.add(new AjaxFormComponentUpdatingBehavior("change") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					onPropertyUpdating(target);
				}

			});
		}
    }

	@Override
	protected Enum<?> convertInputToValue() throws ConversionException {
		String convertedInput = input.getConvertedInput();
		if (convertedInput != null) 
			return Enum.valueOf(enumClass, convertedInput);
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
