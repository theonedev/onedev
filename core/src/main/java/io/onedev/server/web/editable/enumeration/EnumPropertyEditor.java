package io.onedev.server.web.editable.enumeration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.util.editable.annotation.ExcludeValues;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class EnumPropertyEditor extends PropertyEditor<Enum<?>> {

	private final EnumSet<?> enumSet;
	
	private DropDownChoice<String> input;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EnumPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Enum<?>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
        enumSet = EnumSet.allOf((Class<Enum>) propertyDescriptor.getPropertyClass());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Collection<String> excludeValues = new HashSet<>();
		ExcludeValues annotation = propertyDescriptor.getPropertyGetter().getAnnotation(ExcludeValues.class);
		if (annotation != null) {
			for (String excludeValue: annotation.value()) {
				excludeValues.add(excludeValue);
			}
		}
		List<String> choices = new ArrayList<>();
        for (Iterator<?> it = enumSet.iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            if (!excludeValues.contains(value.name()))
                choices.add(value.toString());
        }

        String stringValue;
        if (getModelObject() != null)
        	stringValue = getModelObject().toString();
        else
        	stringValue = null;
        
        IChoiceRenderer<String> choiceRenderer = new IChoiceRenderer<String>() {

			@Override
			public Object getDisplayValue(String object) {
				return StringUtils.capitalize(object.replace('_', ' ').toLowerCase());
			}

			@Override
			public String getIdValue(String object, int index) {
				return object;
			}

			@Override
			public String getObject(String id, IModel<? extends List<? extends String>> choices) {
				return id;
			}
			
		};
        input = new DropDownChoice<String>("input", Model.of(stringValue), choices, choiceRenderer);

        input.setNullValid(!getPropertyDescriptor().isPropertyRequired());	
		input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
        
		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
        
        add(input);
    }

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected Enum<?> convertInputToValue() throws ConversionException {
		String stringValue = input.getConvertedInput();
        for (Iterator<?> it = enumSet.iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            if (value.toString().equals(stringValue)) 
            	return value;
        }
        return null;
	}

}
