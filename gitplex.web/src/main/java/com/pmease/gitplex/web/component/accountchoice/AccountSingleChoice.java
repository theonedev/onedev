package com.pmease.gitplex.web.component.accountchoice;

import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.assets.accountchoice.AbstractAccountSingleChoice;

@SuppressWarnings("serial")
public class AccountSingleChoice extends AbstractAccountSingleChoice {

	public AccountSingleChoice(String id, IModel<Account> model, boolean allowEmpty) {
		super(id, model, new AccountChoiceProvider(), allowEmpty);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose an account ...");
	}

}
