package io.onedev.server.web.editable.choice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.component.select2.Select2Choice;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class SingleChoiceEditor extends PropertyEditor<String> {

	private List<String> choices = new ArrayList<>();
	
	private Select2Choice<String> input;
	
	public SingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		OneContext oneContext = new ComponentContext(this);
		
		OneContext.push(oneContext);
		try {
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
		
		input = new StringSingleChoice("input", Model.of(getModelObject()), choices);
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
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
