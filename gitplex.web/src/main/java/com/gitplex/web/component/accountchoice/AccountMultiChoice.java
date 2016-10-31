package com.gitplex.web.component.accountchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.core.entity.Account;
import com.gitplex.web.component.accountchoice.AccountChoiceResourceReference;
import com.gitplex.commons.wicket.component.select2.Select2MultiChoice;

public class AccountMultiChoice extends Select2MultiChoice<Account> {

	private static final long serialVersionUID = 1L;

	public AccountMultiChoice(String id, IModel<Collection<Account>> model, 
			AbstractAccountChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose accounts ...");
		getSettings().setFormatResult("gitplex.accountChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.accountChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.accountChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new AccountChoiceResourceReference()));
	}

}
