package com.gitplex.server.web.editable.groupchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.web.component.groupchoice.GroupChoiceProvider;
import com.gitplex.server.web.component.groupchoice.GroupSingleChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class GroupSingleChoiceEditor extends PropertyEditor<String> {
	
	private GroupSingleChoice input;
	
	public GroupSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Group group;
		if (getModelObject() != null)
			group = GitPlex.getInstance(GroupManager.class).find(getModelObject());
		else
			group = null;
		
    	input = new GroupSingleChoice("input", Model.of(group), new GroupChoiceProvider());
        input.setConvertEmptyInputStringToNull(true);

        // add this to control allowClear flag of select2
    	input.setRequired(propertyDescriptor.isPropertyRequired());
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
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
