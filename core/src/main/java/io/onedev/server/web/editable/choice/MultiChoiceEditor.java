package io.onedev.server.web.editable.choice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	private final List<String> choices = new ArrayList<>();
	
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
			getPropertyDescriptor().getDependencyPropertyNames().clear();
			io.onedev.server.util.editable.annotation.ChoiceProvider choiceProvider = 
					propertyDescriptor.getPropertyGetter().getAnnotation(
							io.onedev.server.util.editable.annotation.ChoiceProvider.class);
			Preconditions.checkNotNull(choiceProvider);
			for (String each: (List<String>)ReflectionUtils
					.invokeStaticMethod(propertyDescriptor.getBeanClass(), choiceProvider.value())) {
				choices.add(each);
			}
		} finally {
			OneContext.pop();
		}
		
        Collection<String> selections = new ArrayList<>();
        if (getModelObject() != null) {
        	for (String each: getModelObject())
        		selections.add(each);
        }
        
		IModel<Collection<String>> model = new Model((Serializable) selections);
        
		input = new StringMultiChoice("input", model, choices) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (!propertyDescriptor.isPropertyRequired() && propertyDescriptor.getNameOfEmptyValue() != null)
					getSettings().setPlaceholder(propertyDescriptor.getNameOfEmptyValue());
			}
			
		};
        input.setLabel(Model.of(getPropertyDescriptor().getDisplayName(this)));
        input.setRequired(propertyDescriptor.isPropertyRequired());
        
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
			values.add(each);
		return values;
	}

}
