package io.onedev.server.web.editable.milestonechoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.milestone.choice.MilestoneMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.MilestoneChoice;

@SuppressWarnings("serial")
public class MilestoneMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private MilestoneMultiChoice input;
	
	public MilestoneMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({"unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Milestone> choices = new ArrayList<>();
		List<Milestone> selections = new ArrayList<>();
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			MilestoneChoice milestoneChoice = descriptor.getPropertyGetter().getAnnotation(MilestoneChoice.class);
			Preconditions.checkNotNull(milestoneChoice);
			if (milestoneChoice.value().length() != 0) {
				choices.addAll((List<Milestone>)ReflectionUtils
						.invokeStaticMethod(descriptor.getBeanClass(), milestoneChoice.value()));
			} else if (Project.get() != null) {
				choices.addAll(Project.get().getSortedHierarchyMilestones());
			}

			if (getModelObject() != null && Project.get() != null) {
				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				for (String milestoneName: getModelObject()) {
					Milestone milestone = milestoneManager.findInHierarchy(Project.get(), milestoneName);
					if (milestone != null && choices.contains(milestone))
						selections.add(milestone);
				}
			} 
		} finally {
			ComponentContext.pop();
		}
		
		input = new MilestoneMultiChoice("input", Model.of(selections), Model.of(choices)) {

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
		Collection<Milestone> milestones = input.getConvertedInput();
		if (milestones != null) 
			return milestones.stream().map(it->it.getName()).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
