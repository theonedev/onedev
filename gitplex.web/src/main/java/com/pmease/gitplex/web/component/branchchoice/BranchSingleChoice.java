package com.pmease.gitplex.web.component.branchchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.vaynberg.wicket.select2.ChoiceProvider;

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
		getSettings().setFormatResult("gitplex.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.branchChoiceFormatter.escapeMarkup");
		
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
		response.render(JavaScriptHeaderItem.forReference(BranchChoiceResourceReference.INSTANCE));
	}
	
}