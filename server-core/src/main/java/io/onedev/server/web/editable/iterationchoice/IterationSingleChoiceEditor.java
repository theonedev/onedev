package io.onedev.server.web.editable.iterationchoice;

import com.google.common.base.Preconditions;
import io.onedev.server.annotation.IterationChoice;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.iteration.choice.IterationSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

public class IterationSingleChoiceEditor extends PropertyEditor<String> {

	private IterationSingleChoice input;
	
	public IterationSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor,
									   IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Iteration> choices = new ArrayList<>();
		Iteration selection = null;
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			IterationChoice iterationChoice = descriptor.getPropertyGetter().getAnnotation(IterationChoice.class);
			Preconditions.checkNotNull(iterationChoice);
			if (iterationChoice.value().length() != 0) {
				choices.addAll((List<Iteration>)ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), iterationChoice.value()));
			} else if (Project.get() != null) {
				choices.addAll(Project.get().getSortedHierarchyIterations());
			}

			for (var choice: choices) {
				if (choice.getName().equals(getModelObject())) {
					selection = choice;
					break;
				}
			}
		} finally {
			ComponentContext.pop();
		}
		
    	input = new IterationSingleChoice("input", Model.of(selection), Model.of(choices)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
				getSettings().setAllowClear(!descriptor.isPropertyRequired());
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
	protected String convertInputToValue() throws ConversionException {
		Iteration iteration = input.getConvertedInput();
		if (iteration != null)
			return iteration.getName();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
