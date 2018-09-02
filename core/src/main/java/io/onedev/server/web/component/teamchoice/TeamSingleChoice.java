package io.onedev.server.web.component.teamchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class TeamSingleChoice extends Select2Choice<TeamFacade> {

	public TeamSingleChoice(String id, IModel<TeamFacade> teamModel, 
			AbstractTeamChoiceProvider choiceProvider) {
		super(id, teamModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose a team ...");
		getSettings().setFormatResult("onedev.server.teamChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.teamChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.teamChoiceFormatter.escapeMarkup");
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