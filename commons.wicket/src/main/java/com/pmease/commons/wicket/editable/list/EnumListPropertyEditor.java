package com.pmease.commons.wicket.editable.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.json.JSONException;
import org.json.JSONWriter;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class EnumListPropertyEditor extends PropertyEditor<List<Enum<?>>> {

	private final EnumSet<?> enumSet;
	
	private Select2MultiChoice<String> input;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EnumListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Enum<?>>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		Class<Enum> enumClass = (Class<Enum>) EditableUtils.getElementClass(
				propertyDescriptor.getPropertyGetter().getGenericReturnType());		
        enumSet = EnumSet.allOf(enumClass);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final List<String> choices = new ArrayList<>();
        for (Iterator<?> it = enumSet.iterator(); it.hasNext();) {
            Enum<?> value = (Enum<?>) it.next();
            choices.add(value.toString());
        }

        Collection<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (Enum<?> each: getModelObject())
        		selections.add(each.toString());
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        		getSettings().setPlaceholder("Select strategies...");
        		getSettings().setFormatResult("pmease.commons.choiceFormatter.id.formatResult");
        		getSettings().setFormatSelection("pmease.commons.choiceFormatter.id.formatSelection");
        		getSettings().setEscapeMarkup("pmease.commons.choiceFormatter.id.escapeMarkup");
        		
        		setConvertEmptyInputStringToNull(true);
        	}
        	
        };

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
	        for (Iterator<?> it = enumSet.iterator(); it.hasNext();) {
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
