package com.pmease.gitplex.web.component.branch;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class BranchMultiChoice extends Select2MultiChoice<RepoAndBranch> {

	public BranchMultiChoice(String id, IModel<Collection<RepoAndBranch>> model, ChoiceProvider<RepoAndBranch> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Start typing to find branches ...");
		getSettings().setFormatResult("gitplex.choiceFormatter.branch.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.branch.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.branch.escapeMarkup");
		
		setConvertEmptyInputStringToNull(true);
	}

}
