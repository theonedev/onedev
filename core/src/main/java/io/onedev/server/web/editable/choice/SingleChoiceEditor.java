package io.onedev.server.web.editable.choice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ReflectionUtils;

@SuppressWarnings("serial")
public class SingleChoiceEditor extends PropertyEditor<String> {

	private static final int SELECT2_THRESHOLD = 10;
	
	private List<String> choices = new ArrayList<>();
	
	private FormComponent<String> input;
	
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

		if (choices.size() > SELECT2_THRESHOLD) {
			input = new StringSingleChoice("input", Model.of(getModelObject()), choices);
		} else {
			input = new DropDownChoice<String>("input", Model.of(getModelObject()), choices) {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					tag.setName("select");
					tag.remove("type");
					super.onComponentTag(tag);
				}

				@Override
				protected String getNullValidDisplayValue() {
					if (propertyDescriptor.isPropertyRequired())
						return "Please choose...";
					else
						return super.getNullValidDisplayValue();
				}
				
			}.setNullValid(true);
		}
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
