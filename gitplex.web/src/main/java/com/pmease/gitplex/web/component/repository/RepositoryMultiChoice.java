package com.pmease.gitplex.web.component.repository;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitplex.core.model.Repository;
import com.vaynberg.wicket.select2.ChoiceProvider;

public class RepositoryMultiChoice extends Select2MultiChoice<Repository> {
	private static final long serialVersionUID = 1L;

	public RepositoryMultiChoice(String id, IModel<Collection<Repository>> model, ChoiceProvider<Repository> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Start typing to find repositories ...");
		getSettings().setFormatResult("gitplex.choiceFormatter.repository.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.repository.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.repository.escapeMarkup");
	}

}
