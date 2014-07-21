package com.pmease.gitplex.web.editable.user;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.UserSingleChoice;

@SuppressWarnings("serial")
public class UserSingleChoiceEditor extends PropertyEditor<Long> {

	public UserSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<Long> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	private UserSingleChoice input;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		User user;
		if (getModelObject() != null)
			user = GitPlex.getInstance(Dao.class).load(User.class, getModelObject());
		else
			user = null;
		
    	input = new UserSingleChoice("input", Model.of(user));
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected Long convertInputToValue() throws ConversionException {
		User user = input.getConvertedInput();
		if (user != null)
			return user.getId();
		else
			return null;
	}

}
