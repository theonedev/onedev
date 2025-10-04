package io.onedev.server.web.editable.rolechoice;

import static io.onedev.server.web.translation.Translation._T;

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
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.component.rolechoice.RoleMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class RoleMultiChoiceEditor extends PropertyEditor<Collection<String>> {
	
	private RoleMultiChoice input;
	
	public RoleMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Collection<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Role> choices = new ArrayList<>();
		
		ComponentContext componentContext = new ComponentContext(this);
		ComponentContext.push(componentContext);
		try {
			RoleChoice roleChoice = descriptor.getPropertyGetter().getAnnotation(RoleChoice.class);
			Preconditions.checkNotNull(roleChoice);
			choices.addAll(OneDev.getInstance(RoleService.class).query());
			choices.sort(Comparator.comparing(Role::getName));
		} finally {
			ComponentContext.pop();			
		}
	
    	List<Role> selections = new ArrayList<>();
		if (getModelObject() != null) {
			RoleService roleService = OneDev.getInstance(RoleService.class);
			for (String roleName: getModelObject()) {
				Role role = roleService.find(roleName);
				if (role != null && choices.contains(role))
					selections.add(role);
			}
		} 
		
		input = new RoleMultiChoice("input", Model.of(selections), Model.of(choices)) {

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
		List<String> roleNames = new ArrayList<>();
		Collection<Role> roles = input.getConvertedInput();
		if (roles != null) {
			for (Role role: roles)
				roleNames.add(role.getName());
		} 
		return roleNames;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
