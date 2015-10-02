package com.pmease.commons.wicket.editable.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.json.JSONException;
import org.json.JSONWriter;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.ReflectionUtils;
import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Response;

@SuppressWarnings("serial")
public class StringListPropertyEditor extends PropertyEditor<List<String>> {

	private final List<String> choices = new ArrayList<>();
	
	private Select2MultiChoice<String> input;
	
	@SuppressWarnings("unchecked")
	public StringListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
		
		com.pmease.commons.wicket.editable.annotation.ChoiceProvider choiceProvider = 
				propertyDescriptor.getPropertyGetter().getAnnotation(
						com.pmease.commons.wicket.editable.annotation.ChoiceProvider.class);
		Preconditions.checkNotNull(choiceProvider);
		for (String each: (List<String>)ReflectionUtils
				.invokeStaticMethod(propertyDescriptor.getBeanClass(), choiceProvider.value())) {
			choices.add(each);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
        Collection<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (String each: getModelObject())
        		selections.add(each);
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
        		getSettings().setPlaceholder("Select below...");
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> values = new ArrayList<>();
		for (String each: input.getConvertedInput())
			values.add(each);
		return values;
	}

}
