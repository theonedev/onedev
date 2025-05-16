package io.onedev.server.web.editable.iterationchoice;

import com.google.common.base.Preconditions;
import io.onedev.server.annotation.IterationChoice;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.iteration.choice.IterationMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class IterationMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private IterationMultiChoice input;
	
	public IterationMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor,
									  IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({"unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Iteration> choices = new ArrayList<>();
		List<Iteration> selections = new ArrayList<>();
		
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
				if (getModelObject() != null && getModelObject().contains(choice.getName()))
					selections.add(choice);
			}
		} finally {
			ComponentContext.pop();
		}
		
		input = new IterationMultiChoice("input", Model.of(selections), Model.of(choices)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
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
	protected List<String> convertInputToValue() throws ConversionException {
		Collection<Iteration> iterations = input.getConvertedInput();
		if (iterations != null) 
			return iterations.stream().map(it->it.getName()).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
