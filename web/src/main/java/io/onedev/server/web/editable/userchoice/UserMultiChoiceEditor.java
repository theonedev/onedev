package io.onedev.server.web.editable.userchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.UserChoice;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.userchoice.UserChoiceProvider;
import io.onedev.server.web.component.userchoice.UserMultiChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

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
			UserManager userManager = OneDev.getInstance(UserManager.class);
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
