package io.onedev.server.web.editable.enumlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.utils.WordUtils;

@SuppressWarnings({"serial", "unchecked", "rawtypes"})
public class EnumListPropertyEditor extends PropertyEditor<List<Enum<?>>> {

	private final Class<Enum> enumClass;
	
	private Select2MultiChoice<String> input;
	
	public EnumListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Enum<?>>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		enumClass = (Class<Enum>) propertyDescriptor.getPropertyGetter().getReturnType();		
	}

	private String getDisplayValue(String choice) {
		return StringUtils.capitalize(choice.replace('_', ' ').toLowerCase());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<String> choices = new ArrayList<>();
        for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            choices.add(getDisplayValue(value.toString()));
        }

        List<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (Enum<?> each: getModelObject())
        		selections.add(getDisplayValue(each.toString()));
        }
		IModel<Collection<String>> model = new Model((Serializable) selections);
        
        input = new StringMultiChoice("input", model, choices) {

        	@Override
        	protected void onInitialize() {
        		super.onInitialize();
        		if (propertyDescriptor.isPropertyRequired()) 
					getSettings().setPlaceholder("Select " + WordUtils.uncamel(enumClass.getSimpleName()).toLowerCase() + "(s)...");
        		else if (propertyDescriptor.getNameOfEmptyValue() != null)
					getSettings().setPlaceholder(propertyDescriptor.getNameOfEmptyValue());
        	}
        	
        };
        input.setRequired(propertyDescriptor.isPropertyRequired());
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
	protected List<Enum<?>> convertInputToValue() throws ConversionException {
		List<Enum<?>> values = new ArrayList<>();
		
		for (String stringValue: input.getConvertedInput()) {
	        for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext();) {
	            Enum<?> value = (Enum<?>) it.next();
	            if (getDisplayValue(value.toString()).equals(stringValue)) { 
	            	values.add(value);
	            	break;
	            }
	        }
		}
        return values;
	}

}
