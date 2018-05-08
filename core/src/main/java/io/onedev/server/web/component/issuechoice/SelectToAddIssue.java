package io.onedev.server.web.component.issuechoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.select2.SelectToAddChoice;

@SuppressWarnings("serial")
public abstract class SelectToAddIssue extends SelectToAddChoice<Issue> {

	public SelectToAddIssue(String id, IModel<Project> projectModel) {
		super(id, new IssueChoiceProvider(projectModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Select issue ...");
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