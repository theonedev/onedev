package com.gitplex.web.editable.accountchoice;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.Account;
import com.gitplex.core.manager.AccountManager;
import com.gitplex.web.component.accountchoice.AccountChoiceProvider;
import com.gitplex.web.component.accountchoice.AccountSingleChoice;
import com.gitplex.commons.wicket.editable.ErrorContext;
import com.gitplex.commons.wicket.editable.PathSegment;
import com.gitplex.commons.wicket.editable.PropertyDescriptor;
import com.gitplex.commons.wicket.editable.PropertyEditor;

@SuppressWarnings("serial")
public class AccountSingleChoiceEditor extends PropertyEditor<String> {

	private AccountSingleChoice input;
	
	private boolean organization;
	
	public AccountSingleChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<String> propertyModel, boolean organization) {
		super(id, propertyDescriptor, propertyModel);
		this.organization = organization;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Account account;
		if (getModelObject() != null)
			account = GitPlex.getInstance(AccountManager.class).findByName(getModelObject());
		else
			account = null;
		
    	input = new AccountSingleChoice("input", Model.of(account), new AccountChoiceProvider(organization));
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
