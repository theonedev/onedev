package com.turbodev.server.web.component.branchchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.turbodev.server.web.component.select2.ChoiceProvider;
import com.turbodev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class BranchSingleChoice extends Select2Choice<String> {

	public BranchSingleChoice(String id, IModel<String> model, 
			ChoiceProvider<String> branchesProvider) {
		super(id, model, branchesProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Choose a branch ...");
		getSettings().setFormatResult("turbodev.server.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("turbodev.server.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("turbodev.server.branchChoiceFormatter.escapeMarkup");
		
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