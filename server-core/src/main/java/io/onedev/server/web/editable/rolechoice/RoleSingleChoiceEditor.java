package io.onedev.server.web.editable.rolechoice;

import static io.onedev.server.web.translation.Translation._T;

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
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.web.component.rolechoice.RoleSingleChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

public class RoleSingleChoiceEditor extends PropertyEditor<String> {
	
	private RoleSingleChoice input;
	
	public RoleSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
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
		Role role;
		if (getModelObject() != null)
			role = OneDev.getInstance(RoleService.class).find(getModelObject());
		else
			role = null;
		
		if (role != null && !choices.contains(role))
			role = null;

    	input = new RoleSingleChoice("input", Model.of(role), Model.of(choices)) {

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
		Role role = input.getConvertedInput();
		if (role != null)
			return role.getName();
		else
			return null;
	}

	@Override
	public boolean needExplicitSubmit() {
		return false;
	}

}
