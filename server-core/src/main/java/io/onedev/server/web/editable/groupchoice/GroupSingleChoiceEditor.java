package io.onedev.server.web.editable.groupchoice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.component.groupchoice.GroupSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.GroupChoice;

@SuppressWarnings("serial")
public class GroupSingleChoiceEditor extends PropertyEditor<String> {
	
	private GroupSingleChoice input;
	
	public GroupSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Group> choices = new ArrayList<>();
		
		GroupChoice groupChoice = descriptor.getPropertyGetter().getAnnotation(GroupChoice.class);
		Preconditions.checkNotNull(groupChoice);
		if (groupChoice.value().length() != 0) {
			choices.addAll((List<Group>)ReflectionUtils
					.invokeStaticMethod(descriptor.getBeanClass(), groupChoice.value()));
		} else {
			choices.addAll(OneDev.getInstance(GroupManager.class).query());
			choices.sort(Comparator.comparing(Group::getName));
		}
		
		Group group;
		if (getModelObject() != null)
			group = OneDev.getInstance(GroupManager.class).find(getModelObject());
		else
			group = null;
		
		if (group != null && !choices.contains(group))
			group = null;

    	input = new GroupSingleChoice("input", Model.of(group), Model.of(choices)) {

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
		Group group = input.getConvertedInput();
		if (group != null)
			return group.getName();
		else
			return null;
	}

}
