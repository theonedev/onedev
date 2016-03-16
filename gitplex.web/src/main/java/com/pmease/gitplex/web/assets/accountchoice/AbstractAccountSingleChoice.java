package com.pmease.gitplex.web.assets.accountchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.assets.accountchoice.AccountChoiceResourceReference;

@SuppressWarnings("serial")
public class AbstractAccountSingleChoice extends Select2Choice<Account> {

	private final boolean allowEmpty;
	
	public AbstractAccountSingleChoice(String id, IModel<Account> model, 
			AbstractAccountChoiceProvider choiceProvider, boolean allowEmpty) {
		super(id, model, choiceProvider);
		
		this.allowEmpty = allowEmpty;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(allowEmpty);
		getSettings().setFormatResult("gitplex.accountChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.accountChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.accountChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(AccountChoiceResourceReference.INSTANCE));
	}
	
}
