package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.core.model.Team;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2MultiChoice;

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
