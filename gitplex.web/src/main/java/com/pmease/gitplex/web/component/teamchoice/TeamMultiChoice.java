package com.pmease.gitplex.web.component.teamchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitplex.core.entity.Account;

@SuppressWarnings("serial")
public class TeamMultiChoice extends Select2MultiChoice<String> {

	public TeamMultiChoice(String id, IModel<Account> organizationModel, IModel<Collection<String>> teamsModel) {
		super(id, teamsModel, new TeamChoiceProvider(organizationModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose teams ...");
		getSettings().setFormatResult("gitplex.teamChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.teamChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.teamChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(TeamChoiceResourceReference.INSTANCE));
	}

}
