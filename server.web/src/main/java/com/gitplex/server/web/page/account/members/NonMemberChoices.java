package com.gitplex.server.web.page.account.members;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.commons.wicket.component.select2.Select2MultiChoice;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.web.component.accountchoice.AccountChoiceResourceReference;

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
		getSettings().setFormatResult("gitplex.server.accountChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.accountChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.accountChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new AccountChoiceResourceReference()));
	}

}
