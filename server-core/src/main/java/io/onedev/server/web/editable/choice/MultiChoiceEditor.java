package io.onedev.server.web.editable.choice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.ChoiceProvider;

@SuppressWarnings("serial")
public class MultiChoiceEditor extends PropertyEditor<List<String>> {

	private Select2MultiChoice<String> input;
	
	public MultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private ChoiceProvider getChoiceProvider() {
		return Preconditions.checkNotNull(descriptor.getPropertyGetter().getAnnotation(ChoiceProvider.class));
	}
	
	@SuppressWarnings({"unchecked"})
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<Map<String, String>> choicesModel = new LoadableDetachableModel<Map<String, String>>() {

			@Override
			protected Map<String, String> load() {
				Map<String, String> choices;
				
				ComponentContext componentContext = new ComponentContext(MultiChoiceEditor.this);
				
				ComponentContext.push(componentContext);
				try {
					Object result = ReflectionUtils.invokeStaticMethod(descriptor.getBeanClass(), getChoiceProvider().value());
					if (result instanceof List) {
						choices = new LinkedHashMap<>();
						for (String each: (List<String>)result) 
							choices.put(each, each);
					} else {
						choices = (Map<String, String>)result;
					}
				} finally {
					ComponentContext.pop();
				}
				
				return choices;
			}
			
		};
		
		List<String> selections = getModelObject();
		
		/*
		 * Avoid loading choicesModel which might be time-consuming. Otherwise, when this component 
		 * is re-created due to a dependency property updating, there is a high chance we are 
		 * operating on the old select2 component and may cause UI clutter 
		 */
		/*
		if (selections != null) 
			selections.retainAll(choicesModel.getObject().keySet());
		else
			selections = new ArrayList<>();
		*/
		if (selections == null) 
			selections = new ArrayList<>();
		input = new StringMultiChoice("input", Model.of(selections), choicesModel, getChoiceProvider().tagsMode()) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
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
	protected List<String> convertInputToValue() throws ConversionException {
		Collection<String> convertedInput = input.getConvertedInput();
		if (convertedInput != null)
			return new ArrayList<>(convertedInput);
		else
			return new ArrayList<>();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
