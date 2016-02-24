package com.pmease.gitplex.web.component.teamchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.entity.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class TeamSingleChoice extends Select2Choice<Team> {

	private final boolean allowEmpty;
	
	public TeamSingleChoice(String id, IModel<Team> model, ChoiceProvider<Team> teamsProvider, boolean allowEmpty) {
		super(id, model, teamsProvider);
		
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