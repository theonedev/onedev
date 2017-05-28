package com.gitplex.server.web.editable.userchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.util.editable.annotation.UserChoice;
import com.gitplex.server.web.component.userchoice.UserChoiceProvider;
import com.gitplex.server.web.component.userchoice.UserSingleChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class UserSingleChoiceEditor extends PropertyEditor<String> {

	private UserSingleChoice input;
	
	private UserChoice.Type type;
	
	public UserSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel, UserChoice.Type type) {
		super(id, propertyDescriptor, propertyModel);
		this.type = type;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		User user;
		if (getModelObject() != null)
			user = GitPlex.getInstance(UserManager.class).findByName(getModelObject());
		else
			user = null;
		
    	input = new UserSingleChoice("input", Model.of(user), new UserChoiceProvider(type));
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
		User user = input.getConvertedInput();
		if (user != null)
			return user.getName();
		else
			return null;
	}

}
