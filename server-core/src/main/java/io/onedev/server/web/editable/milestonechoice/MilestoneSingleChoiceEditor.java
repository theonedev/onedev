package io.onedev.server.web.editable.milestonechoice;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.milestone.choice.MilestoneSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.MilestoneChoice;

@SuppressWarnings("serial")
public class MilestoneSingleChoiceEditor extends PropertyEditor<String> {

	private MilestoneSingleChoice input;
	
	public MilestoneSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Milestone> choices = new ArrayList<>();
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			MilestoneChoice milestoneChoice = descriptor.getPropertyGetter().getAnnotation(MilestoneChoice.class);
			Preconditions.checkNotNull(milestoneChoice);
			if (milestoneChoice.value().length() != 0) {
				choices.addAll((List<Milestone>)ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), milestoneChoice.value()));
			} else {
				choices.addAll(Project.get().getSortedMilestones());
			}
		} finally {
			ComponentContext.pop();
		}
		
		Milestone selection;
		if (getModelObject() != null)
			selection = Project.get().getMilestone(getModelObject());
		else
			selection = null;
		
		if (selection != null && !choices.contains(selection))
			selection = null;
		
    	input = new MilestoneSingleChoice("input", Model.of(selection), Model.of(choices)) {

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
		Milestone milestone = input.getConvertedInput();
		if (milestone != null)
			return milestone.getName();
		else
			return null;
	}

}
