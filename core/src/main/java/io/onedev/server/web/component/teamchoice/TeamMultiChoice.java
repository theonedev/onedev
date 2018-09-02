package io.onedev.server.web.component.teamchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class TeamMultiChoice extends Select2MultiChoice<TeamFacade> {

	public TeamMultiChoice(String id, IModel<Collection<TeamFacade>> teamsModel, 
			AbstractTeamChoiceProvider choiceProvider) {
		super(id, teamsModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose teams ...");
		getSettings().setFormatResult("onedev.server.teamChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.teamChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.teamChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new TeamChoiceResourceReference()));
	}

}
