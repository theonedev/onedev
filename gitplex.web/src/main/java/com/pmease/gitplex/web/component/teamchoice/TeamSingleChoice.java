package com.pmease.gitplex.web.component.teamchoice;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class TeamSingleChoice extends Select2Choice<Team> {

	public TeamSingleChoice(String id, IModel<Team> model,
			ChoiceProvider<Team> teamsProvider) {
		super(id, model, teamsProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a team ...");
		getSettings().setFormatResult("gitplex.choiceFormatter.team.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.team.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.team.escapeMarkup");
	}

}