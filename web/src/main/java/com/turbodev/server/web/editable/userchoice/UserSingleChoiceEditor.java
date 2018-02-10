package com.turbodev.server.web.editable.userchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.UserChoice;
import com.turbodev.server.util.facade.UserFacade;
import com.turbodev.server.web.component.userchoice.UserChoiceProvider;
import com.turbodev.server.web.component.userchoice.UserSingleChoice;
import com.turbodev.server.web.editable.ErrorContext;
import com.turbodev.server.web.editable.PathSegment;
import com.turbodev.server.web.editable.PropertyDescriptor;
import com.turbodev.server.web.editable.PropertyEditor;

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
			user = TurboDev.getInstance(UserManager.class).findByName(getModelObject());
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
