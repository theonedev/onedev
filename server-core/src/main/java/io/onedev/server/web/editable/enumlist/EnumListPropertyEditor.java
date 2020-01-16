package io.onedev.server.web.editable.enumlist;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings({"serial", "unchecked", "rawtypes"})
public class EnumListPropertyEditor extends PropertyEditor<List<Enum<?>>> {

	private final Class<Enum> enumClass;
	
	private Select2MultiChoice<String> input;
	
	public EnumListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Enum<?>>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		enumClass = (Class<Enum>) ReflectionUtils.getCollectionElementType(propertyDescriptor.getPropertyGetter().getGenericReturnType());
	}

	private String getDisplayValue(String choice) {
		return StringUtils.capitalize(choice.replace('_', ' ').toLowerCase());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
        List<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (Enum<?> each: getModelObject()) 
        		selections.add(each.name());
        }
        
        input = new StringMultiChoice("input", Model.of(selections), new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices = new LinkedHashMap<>();
		        for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext();) {
		            Enum<?> value = (Enum<?>) it.next();
		            choices.put(value.name(), getDisplayValue(value.toString()));
		        }
		        return choices;
			}
        	
        }) {

        	@Override
        	protected void onInitialize() {
        		super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
        	}
        	
        };
        
        input.setRequired(descriptor.isPropertyRequired());
        input.setLabel(Model.of(getDescriptor().getDisplayName()));

		input.add(new AjaxFormComponentUpdatingBehavior("change"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
        add(input);
    }

	@Override
	protected List<Enum<?>> convertInputToValue() throws ConversionException {
		List<Enum<?>> values = new ArrayList<>();
		if (input.getConvertedInput() != null) {
			for (String each: input.getConvertedInput())
				values.add(Enum.valueOf(enumClass, each));
		}
        return values;
	}

}
