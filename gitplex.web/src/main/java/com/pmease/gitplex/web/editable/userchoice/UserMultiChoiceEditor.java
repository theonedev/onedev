package com.pmease.gitplex.web.editable.userchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.component.userchoice.UserMultiChoice;

@SuppressWarnings("serial")
public class UserMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private UserMultiChoice input;
	
	public UserMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	List<Account> users = new ArrayList<>();
		if (getModelObject() != null) {
			AccountManager userManager = GitPlex.getInstance(AccountManager.class);
			for (String userName: getModelObject()) {
				Account user = userManager.findByName(userName);
				if (user != null)
					users.add(user);
			}
		} 
		
		input = new UserMultiChoice("input", new Model((Serializable)users));
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
		Collection<Account> useres = input.getConvertedInput();
		if (useres != null) {
			for (Account user: useres)
				userNames.add(user.getName());
		} 
		return userNames;
	}

}
