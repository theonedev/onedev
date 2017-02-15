package com.gitplex.server.web.editable.accountchoice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.Account;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.web.component.accountchoice.AccountChoiceProvider;
import com.gitplex.server.web.component.accountchoice.AccountMultiChoice;
import com.gitplex.server.web.editable.ErrorContext;
import com.gitplex.server.web.editable.PathSegment;
import com.gitplex.server.web.editable.PropertyDescriptor;
import com.gitplex.server.web.editable.PropertyEditor;

@SuppressWarnings("serial")
public class AccountMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private AccountMultiChoice input;
	
	private final boolean organization;
	
	public AccountMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, 
			IModel<List<String>> propertyModel, boolean organization) {
		super(id, propertyDescriptor, propertyModel);
		this.organization = organization;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
    	List<Account> accounts = new ArrayList<>();
		if (getModelObject() != null) {
			AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
			for (String accountName: getModelObject()) {
				Account account = accountManager.findByName(accountName);
				if (account != null)
					accounts.add(account);
			}
		} 
		
		input = new AccountMultiChoice("input", new Model((Serializable)accounts), new AccountChoiceProvider(organization));
        input.setConvertEmptyInputStringToNull(true);
        
        add(input);
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> accountNames = new ArrayList<>();
		Collection<Account> accounts = input.getConvertedInput();
		if (accounts != null) {
			for (Account account: accounts)
				accountNames.add(account.getName());
		} 
		return accountNames;
	}

}
