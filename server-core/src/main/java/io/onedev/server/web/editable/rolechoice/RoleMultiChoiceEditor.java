package io.onedev.server.web.editable.rolechoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.web.component.stringchoice.StringMultiChoice;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class RoleMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private StringMultiChoice input;
	
	public RoleMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Map<String, String> roleNames = new LinkedHashMap<>();
		for (Role role: OneDev.getInstance(RoleManager.class).query())
			roleNames.put(role.getName(), role.getName());
		
		Collection<String> selections = new ArrayList<>();
		if (getModelObject() != null)
			selections.addAll(getModelObject());
		
		selections.retainAll(roleNames.keySet());
		
		input = new StringMultiChoice("input", Model.of(selections), Model.ofMap(roleNames)) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().configurePlaceholder(descriptor);
			}
			
		};
        input.setConvertEmptyInputStringToNull(true);
        
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
		Collection<String> roleNames = input.getConvertedInput();
		if (roleNames != null) 
			return new ArrayList<>(roleNames);
		else
			return new ArrayList<>();
	}

}
