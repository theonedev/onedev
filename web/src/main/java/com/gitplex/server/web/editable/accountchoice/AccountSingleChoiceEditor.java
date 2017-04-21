package com.gitplex.server.web.editable.accountchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.util.editable.annotation.AccountChoice;
import com.gitplex.server.web.component.accountchoice.AccountChoiceProvider;
import com.gitplex.server.web.component.accountchoice.AccountSingleChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class AccountSingleChoiceEditor extends PropertyEditor<String> {

	private AccountSingleChoice input;
	
	private AccountChoice.Type type;
	
	public AccountSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel, AccountChoice.Type type) {
		super(id, propertyDescriptor, propertyModel);
		this.type = type;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Account account;
		if (getModelObject() != null)
			account = GitPlex.getInstance(AccountManager.class).findByName(getModelObject());
		else
			account = null;
		
    	input = new AccountSingleChoice("input", Model.of(account), new AccountChoiceProvider(type));
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
		Account account = input.getConvertedInput();
		if (account != null)
			return account.getName();
		else
			return null;
	}

}
