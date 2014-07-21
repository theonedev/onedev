package com.pmease.gitplex.web.component.branch;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Branch;
import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class BranchSingleChoice extends Select2Choice<Branch> {

	public BranchSingleChoice(String id, IModel<Branch> model, ChoiceProvider<Branch> branchesProvider) {
		super(id, model, branchesProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a branch ...");
		getSettings().setFormatResult("gitplex.choiceFormatter.branch.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.branch.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.branch.escapeMarkup");
		
		setConvertEmptyInputStringToNull(true);
	}

}