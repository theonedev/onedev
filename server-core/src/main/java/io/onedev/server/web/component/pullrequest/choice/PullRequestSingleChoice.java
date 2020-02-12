package io.onedev.server.web.component.pullrequest.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class PullRequestSingleChoice extends Select2Choice<PullRequest> {

	public PullRequestSingleChoice(String id, IModel<PullRequest> model, PullRequestChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose pull request...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.pullRequestChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.pullRequestChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.pullRequestChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new PullRequestChoiceResourceReference()));
	}
	
}
