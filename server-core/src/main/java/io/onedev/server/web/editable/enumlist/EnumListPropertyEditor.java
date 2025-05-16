package io.onedev.server.web.editable.enumlist;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import io.onedev.server.web.util.TextUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumListPropertyEditor extends PropertyEditor<List<Enum<?>>> {

	private final Class<Enum> enumClass;
	
	private Select2MultiChoice<String> input;
	
	public EnumListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Enum<?>>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		enumClass = (Class<Enum>) ReflectionUtils.getCollectionElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
        List<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (Enum<?> each: getModelObject()) 
        		selections.add(each.name());
        }
        
		var choicesModel = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> choices = new ArrayList<>();
				for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext(); ) {
					choices.add(((Enum<?>) it.next()).name());
				}
				return choices;
			}

		};
		var displayNamesModel = new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> displayNames = new HashMap<>();
				for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext(); ) {
					Enum<?> value = (Enum<?>) it.next();
						displayNames.put(value.name(), TextUtils.getDisplayValue(value));
				}
				return displayNames;
			}

		};

        input = new StringMultiChoice("input", Model.of(selections), choicesModel, displayNamesModel, false) {

        	@Override
        	protected void onInitialize() {
        		super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
        	}
        	
        };
        
        input.setLabel(Model.of(_T(getDescriptor().getDisplayName())));

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

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
