package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2MultiChoice;

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
		getSettings().setFormatResult("gitop.choiceFormatter.repository.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.repository.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.repository.escapeMarkup");
	}

}
