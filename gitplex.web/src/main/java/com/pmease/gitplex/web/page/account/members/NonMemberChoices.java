package com.pmease.gitplex.web.page.account.members;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceResourceReference;

public class NonMemberChoices extends Select2MultiChoice<Account> {

	private static final long serialVersionUID = 1L;

	public NonMemberChoices(String id, IModel<Account> organizationModel, 
			IModel<Collection<Account>> nonMembersModel) {
		super(id, nonMembersModel, new NonMemberChoiceProvider(organizationModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose new members...");
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
