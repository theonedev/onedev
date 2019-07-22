package io.onedev.server.web.component.branch.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class BranchSingleChoice extends Select2Choice<String> {

	public BranchSingleChoice(String id, IModel<String> model, 
			ChoiceProvider<String> branchesProvider) {
		super(id, model, branchesProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (isRequired())
			getSettings().setPlaceholder("Choose a branch ...");
		getSettings().setFormatResult("onedev.server.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.branchChoiceFormatter.escapeMarkup");
		
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
		response.render(JavaScriptHeaderItem.forReference(new BranchChoiceResourceReference()));
	}
	
}