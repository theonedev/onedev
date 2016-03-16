package com.pmease.gitplex.web.component.accountchoice;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.assets.accountchoice.AbstractAccountMultiChoice;

public class AccountMultiChoice extends AbstractAccountMultiChoice {

	private static final long serialVersionUID = 1L;

	public AccountMultiChoice(String id, IModel<Collection<Account>> model) {
		super(id, model, new AccountChoiceProvider());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose accounts ...");
	}

}
