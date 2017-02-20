package com.gitplex.server.web.component.teamchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Team;
import com.gitplex.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class TeamSingleChoice extends Select2Choice<Team> {

	public TeamSingleChoice(String id, IModel<Team> teamModel, 
			AbstractTeamChoiceProvider choiceProvider) {
		super(id, teamModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a team ...");
		getSettings().setFormatResult("gitplex.server.teamChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.teamChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.teamChoiceFormatter.escapeMarkup");
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new TeamChoiceResourceReference()));
	}
	
}