package io.onedev.server.web.editable.userchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.UserChoice;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.component.userchoice.UserChoiceProvider;
import io.onedev.server.web.component.userchoice.UserSingleChoice;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;

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
			user = OneDev.getInstance(UserManager.class).findByName(getModelObject());
		else
			user = null;
		
		UserFacade facade = user!=null?user.getFacade():null;
    	input = new UserSingleChoice("input", Model.of(facade), new UserChoiceProvider(type));
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
		UserFacade user = input.getConvertedInput();
		if (user != null)
			return user.getName();
		else
			return null;
	}

}
