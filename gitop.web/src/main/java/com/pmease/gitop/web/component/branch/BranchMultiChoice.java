package com.pmease.gitop.web.component.branch;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitop.model.Branch;
import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class BranchMultiChoice extends Select2MultiChoice<Branch> {

	public BranchMultiChoice(String id, IModel<Collection<Branch>> model, ChoiceProvider<Branch> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Start typing to find branches ...");
		getSettings().setFormatResult("gitop.choiceFormatter.branch.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.branch.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.branch.escapeMarkup");
		
		setConvertEmptyInputStringToNull(true);
	}

}
