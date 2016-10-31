package com.gitplex.web.component.teamchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.core.entity.Team;
import com.gitplex.commons.wicket.component.select2.Select2Choice;

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
		getSettings().setFormatResult("gitplex.teamChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.teamChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.teamChoiceFormatter.escapeMarkup");
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