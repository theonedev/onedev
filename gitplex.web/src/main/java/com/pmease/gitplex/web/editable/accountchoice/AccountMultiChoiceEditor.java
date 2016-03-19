package com.pmease.gitplex.web.editable.accountchoice;

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
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceProvider;
import com.pmease.gitplex.web.component.accountchoice.AccountMultiChoice;

@SuppressWarnings("serial")
public class AccountMultiChoiceEditor extends PropertyEditor<List<String>> {
	
	private AccountMultiChoice input;
	
	public AccountMultiChoiceEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<String>> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
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
		
		input = new AccountMultiChoice("input", new Model((Serializable)accounts), new AccountChoiceProvider());
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
