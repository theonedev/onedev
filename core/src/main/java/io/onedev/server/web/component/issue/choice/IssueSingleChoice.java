package io.onedev.server.web.component.issue.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Issue;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class IssueSingleChoice extends Select2Choice<Issue> {

	public IssueSingleChoice(String id, IModel<Issue> model, IssueChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Select issue ...");
		getSettings().setFormatResult("onedev.server.issueChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.issueChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.issueChoiceFormatter.escapeMarkup");
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new IssueChoiceResourceReference()));
	}
	
}
