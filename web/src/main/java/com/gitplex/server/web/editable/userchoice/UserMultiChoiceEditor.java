package com.gitplex.server.web.editable.userchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.util.editable.annotation.UserChoice;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.web.component.userchoice.UserChoiceProvider;
import com.gitplex.server.web.component.userchoice.UserMultiChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class UserMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private UserMultiChoice input;
	
	private final UserChoice.Type type;
	
	public UserMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel, UserChoice.Type type) {
		super(id, propertyDescriptor, propertyModel);
		this.type = type;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	List<UserFacade> users = new ArrayList<>();
		if (getModelObject() != null) {
			UserManager userManager = GitPlex.getInstance(UserManager.class);
			for (String userName: getModelObject()) {
				User user = userManager.findByName(userName);
				if (user != null)
					users.add(user.getFacade());
			}
		} 
		
		input = new UserMultiChoice("input", new Model((Serializable)users), new UserChoiceProvider(type));
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> userNames = new ArrayList<>();
		Collection<UserFacade> users = input.getConvertedInput();
		if (users != null) {
			for (UserFacade user: users)
				userNames.add(user.getName());
		} 
		return userNames;
	}

}
