package com.pmease.gitop.web.component.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

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
		getSettings().setFormatResult("TeamChoice.formatter.formatResult");
		getSettings()
				.setFormatSelection("TeamChoice.formatter.formatSelection");
		getSettings().setEscapeMarkup("TeamChoice.formatter.escapeMarkup");
	}

	private ResourceReference teamChoiceReference = 
			new JavaScriptResourceReference(UserSingleChoice.class, "teamchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(teamChoiceReference));
	}
}