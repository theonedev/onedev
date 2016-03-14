package com.pmease.gitplex.web.component.teamchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.entity.Account;

@SuppressWarnings("serial")
public class TeamSingleChoice extends Select2Choice<String> {

	private final boolean allowEmpty;
	
	public TeamSingleChoice(String id, IModel<Account> organizationModel, IModel<String> teamModel, boolean allowEmpty) {
		super(id, teamModel, new TeamChoiceProvider(organizationModel));
		
		this.allowEmpty = allowEmpty;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(allowEmpty);
		getSettings().setPlaceholder("Choose a team ...");
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