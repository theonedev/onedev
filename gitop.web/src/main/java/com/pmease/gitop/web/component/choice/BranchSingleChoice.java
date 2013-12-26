package com.pmease.gitop.web.component.choice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.model.Branch;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;

@SuppressWarnings("serial")
public class BranchSingleChoice extends Select2Choice<Branch> {

	public BranchSingleChoice(String id, IModel<Branch> model,
			ChoiceProvider<Branch> branchesProvider) {
		super(id, model, branchesProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a branch ...");
		getSettings().setFormatResult("BranchChoice.formatter.formatResult");
		getSettings().setFormatSelection("BranchChoice.formatter.formatSelection");
		getSettings().setEscapeMarkup("BranchChoice.formatter.escapeMarkup");
	}

	private ResourceReference branchChoiceReference = 
			new JavaScriptResourceReference(BranchSingleChoice.class, "branchchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(branchChoiceReference));
	}
}