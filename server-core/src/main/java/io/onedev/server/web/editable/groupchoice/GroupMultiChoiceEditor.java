package io.onedev.server.web.editable.groupchoice;

import java.util.ArrayList;
import java.util.Collection;
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
import io.onedev.server.web.component.groupchoice.GroupMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.annotation.GroupChoice;

@SuppressWarnings("serial")
public class GroupMultiChoiceEditor extends PropertyEditor<Collection<String>> {
	
	private GroupMultiChoice input;
	
	public GroupMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Collection<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "unchecked" })
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
	
    	List<Group> selections = new ArrayList<>();
		if (getModelObject() != null) {
			GroupManager groupManager = OneDev.getInstance(GroupManager.class);
			for (String groupName: getModelObject()) {
				Group group = groupManager.find(groupName);
				if (group != null && choices.contains(group))
					selections.add(group);
			}
		} 
		
		input = new GroupMultiChoice("input", Model.of(selections), Model.of(choices)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        
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
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> groupNames = new ArrayList<>();
		Collection<Group> groups = input.getConvertedInput();
		if (groups != null) {
			for (Group group: groups)
				groupNames.add(group.getName());
		} 
		return groupNames;
	}

}
