package io.onedev.server.web.component.branch.choice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;

public class BranchMultiChoice extends Select2MultiChoice<String> {

	public BranchMultiChoice(String id, IModel<Collection<String>> model, 
			IModel<List<String>> choicesModel, boolean tagsMode) {
		super(id, model, new StringChoiceProvider(choicesModel, tagsMode));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose branches..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("onedev.server.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.branchChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BranchChoiceResourceReference()));
	}
	
}
