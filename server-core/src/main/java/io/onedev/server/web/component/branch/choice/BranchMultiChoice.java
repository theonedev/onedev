package io.onedev.server.web.component.branch.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class BranchMultiChoice extends Select2MultiChoice<String> {

	public BranchMultiChoice(String id, IModel<Collection<String>> model, ChoiceProvider<String> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose branches ...");
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
