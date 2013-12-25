package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.model.Branch;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class BranchMultiChoice extends Select2MultiChoice<Branch> {

	public BranchMultiChoice(String id, IModel<Collection<Branch>> model,
			ChoiceProvider<Branch> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Start typing to find branches ...");
		getSettings().setFormatResult("BranchChoice.formatter.formatResult");
		getSettings().setFormatSelection("BranchChoice.formatter.formatSelection");
		getSettings().setEscapeMarkup("BranchChoice.formatter.escapeMarkup");
	}

	private ResourceReference branchChoiceReference = 
			new JavaScriptResourceReference(BranchMultiChoice.class, "branchchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(branchChoiceReference));
	}
}
