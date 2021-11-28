package io.onedev.server.web.component.issue.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.model.Issue;
import io.onedev.server.web.component.select2.SelectToAddChoice;

@SuppressWarnings("serial")
public abstract class IssueAddChoice extends SelectToAddChoice<Issue> {

	public IssueAddChoice(String id, IssueChoiceProvider choiceProvider) {
		super(id, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Add Issue...");
		getSettings().setFormatResult("onedev.server.issueChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.issueChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.issueChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new IssueChoiceResourceReference()));
	}

}
