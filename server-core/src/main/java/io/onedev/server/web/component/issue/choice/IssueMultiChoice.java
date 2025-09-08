package io.onedev.server.web.component.issue.choice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Issue;
import io.onedev.server.web.component.select2.Select2MultiChoice;

public class IssueMultiChoice extends Select2MultiChoice<Issue> {

	public IssueMultiChoice(String id, IModel<Collection<Issue>> model, IssueChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose issues..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("onedev.server.issueChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.issueChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.issueChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new IssueChoiceResourceReference()));
	}
	
}
