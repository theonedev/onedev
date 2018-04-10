package io.onedev.server.web.editable.enumlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.json.JSONException;
import org.json.JSONWriter;

import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.editable.annotation.ExcludeValues;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.Select2MultiChoice;
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
		enumClass = (Class<Enum>) EditableUtils.getElementClass(
				propertyDescriptor.getPropertyGetter().getGenericReturnType());		
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
        for (Iterator<?> it = EnumSet.allOf(enumClass).iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            if (!excludeValues.contains(value.name())) {
            	choices.add(value.toString());
            }
        }

        Collection<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (Enum<?> each: getModelObject())
        		selections.add(each.toString());
        }
		IModel<Collection<String>> model = new Model((Serializable) selections);
        
        input = new Select2MultiChoice<String>("input", model, new ChoiceProvider<String>() {

			@Override
			public void query(String term, int page, Response<String> response) {
				response.setResults(choices);
				response.setHasMore(false);
			}

			@Override
			public void toJson(String choice, JSONWriter writer) throws JSONException {
				writer.key("id").value(StringEscapeUtils.escapeHtml4(choice));
			}

			@Override
			public Collection<String> toChoices(Collection<String> ids) {
				return ids;
			}
        	
        }) {

        	@Override
        	protected void onInitialize() {
        		super.onInitialize();
        		getSettings().setPlaceholder("Select " + WordUtils.uncamel(enumClass.getSimpleName()).toLowerCase() + "(s)...");
        		getSettings().setFormatResult("onedev.server.choiceFormatter.formatResult");
        		getSettings().setFormatSelection("onedev.server.choiceFormatter.formatSelection");
        		getSettings().setEscapeMarkup("onedev.server.choiceFormatter.escapeMarkup");
        		
        		setConvertEmptyInputStringToNull(true);
        	}
        	
        };

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
	            if (value.toString().equals(stringValue)) { 
	            	values.add(value);
	            	break;
	            }
	        }
		}
        return values;
	}

}
