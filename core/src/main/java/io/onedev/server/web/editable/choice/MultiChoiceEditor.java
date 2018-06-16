package io.onedev.server.web.editable.choice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class MultiChoiceEditor extends PropertyEditor<List<String>> {

	private final Map<String, String> choices = new LinkedHashMap<>();
	
	private Select2MultiChoice<String> input;
	
	public MultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		OneContext oneContext = new ComponentContext(this);
		
		OneContext.push(oneContext);
		try {
			getDescriptor().getDependencyPropertyNames().clear();
			io.onedev.server.web.editable.annotation.ChoiceProvider choiceProvider = 
					descriptor.getPropertyGetter().getAnnotation(
							io.onedev.server.web.editable.annotation.ChoiceProvider.class);
			Preconditions.checkNotNull(choiceProvider);
			Object result = ReflectionUtils.invokeStaticMethod(descriptor.getBeanClass(), choiceProvider.value());
			if (result instanceof List) {
				for (String each: (List<String>)result) {
					choices.put(each, each);
				}
			} else {
				choices.putAll(((Map)result));
			}
		} finally {
			OneContext.pop();
		}
		
		Map<String, String> invertedChoices = MapUtils.invertMap(choices);
        Collection<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (String value: getModelObject()) {
        		String key = invertedChoices.get(value);
        		if (key != null)
        			selections.add(key);
        	}
        }
        
		IModel<Collection<String>> model = new Model((Serializable) selections);
        
		input = new StringMultiChoice("input", model, new ArrayList<>(choices.keySet())) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        input.setLabel(Model.of(getDescriptor().getDisplayName(this)));
        input.setRequired(descriptor.isPropertyRequired());
        
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> values = new ArrayList<>();
		for (String each: input.getConvertedInput())
			values.add(choices.get(each));
		return values;
	}

}
