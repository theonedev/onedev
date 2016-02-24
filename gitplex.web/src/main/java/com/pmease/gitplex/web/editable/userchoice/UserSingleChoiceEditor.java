package com.pmease.gitplex.web.editable.userchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.web.component.userchoice.UserSingleChoice;

@SuppressWarnings("serial")
public class UserSingleChoiceEditor extends PropertyEditor<String> {

	private UserSingleChoice input;
	
	public UserSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		User user;
		if (getModelObject() != null)
			user = GitPlex.getInstance(UserManager.class).findByName(getModelObject());
		else
			user = null;
		
    	input = new UserSingleChoice("input", Model.of(user), !getPropertyDescriptor().isPropertyRequired());
        input.setConvertEmptyInputStringToNull(true);
        
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
