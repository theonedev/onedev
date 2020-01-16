package io.onedev.server.web.editable.choice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.stringchoice.StringSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class SingleChoiceEditor extends PropertyEditor<String> {

	private FormComponent<String> input;
	
	public SingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({"unchecked"})
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<Map<String, String>> choicesModel = new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices;
				
				ComponentContext componentContext = new ComponentContext(SingleChoiceEditor.this);
				
				ComponentContext.push(componentContext);
				try {
					io.onedev.server.web.editable.annotation.ChoiceProvider choiceProvider = 
							descriptor.getPropertyGetter().getAnnotation(
									io.onedev.server.web.editable.annotation.ChoiceProvider.class);
					Preconditions.checkNotNull(choiceProvider);
					Object result = ReflectionUtils.invokeStaticMethod(descriptor.getBeanClass(), choiceProvider.value());
					if (result instanceof List) {
						choices = new LinkedHashMap<>();
						for (String each: (List<String>)result) 
							choices.put(each, each);
					} else {
						choices = ((Map<String, String>)result);
					}
				} finally {
					ComponentContext.pop();
				}
				return choices;
			}
			
		};
		
		String selection = getModelObject();
		if (!choicesModel.getObject().containsKey(selection))
			selection = null;
		input = new StringSingleChoice("input", Model.of(selection), choicesModel) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        // add this to control allowClear flag of select2
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
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

}
