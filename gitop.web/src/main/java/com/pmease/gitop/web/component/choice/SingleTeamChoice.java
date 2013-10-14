package com.pmease.gitop.web.component.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.core.model.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;

@SuppressWarnings("serial")
public class SingleTeamChoice extends Select2Choice<Team> {

	public SingleTeamChoice(String id, IModel<Team> model,
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
			new JavaScriptResourceReference(SingleUserChoice.class, "teamchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(teamChoiceReference));
	}
}