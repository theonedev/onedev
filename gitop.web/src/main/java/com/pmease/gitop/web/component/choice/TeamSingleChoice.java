package com.pmease.gitop.web.component.choice;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitop.model.Team;
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
		getSettings().setFormatResult("gitop.choiceFormatter.team.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.team.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.team.escapeMarkup");
	}

}