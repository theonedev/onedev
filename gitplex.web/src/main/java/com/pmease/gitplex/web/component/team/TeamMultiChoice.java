package com.pmease.gitplex.web.component.team;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitplex.core.model.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class TeamMultiChoice extends Select2MultiChoice<Team> {

	public TeamMultiChoice(String id, IModel<Collection<Team>> model,
			ChoiceProvider<Team> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Start typing to find teams ...");
		getSettings().setFormatResult("gitplex.choiceFormatter.team.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.team.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.team.escapeMarkup");
	}

}
