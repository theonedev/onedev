package com.gitplex.server.web.component.teamchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.entity.Team;
import com.gitplex.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class TeamMultiChoice extends Select2MultiChoice<Team> {

	public TeamMultiChoice(String id, IModel<Collection<Team>> teamsModel, 
			AbstractTeamChoiceProvider choiceProvider) {
		super(id, teamsModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose teams ...");
		getSettings().setFormatResult("gitplex.server.teamChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.teamChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.teamChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new TeamChoiceResourceReference()));
	}

}
