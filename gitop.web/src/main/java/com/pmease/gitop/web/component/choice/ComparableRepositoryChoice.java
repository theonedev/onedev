package com.pmease.gitop.web.component.choice;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;
import com.vaynberg.wicket.select2.Select2Choice;

@SuppressWarnings("serial")
public class ComparableRepositoryChoice extends Select2Choice<Repository> {

	/**
	 * Constructor with model.
	 * 
	 * @param id
	 * 			component id of the choice
	 * @param currentRepositoryModel
	 * 			model of current repository from which to calculate comparable repositories. Note that 
	 * 			model.getObject() should never return null
	 * @param selectedRepositoryModel
	 * 			model of selected repository
	 */
	public ComparableRepositoryChoice(String id, IModel<Repository> currentRepositoryModel, IModel<Repository> selectedRepositoryModel) {
		super(id, selectedRepositoryModel, new ComparableRepositoryChoiceProvider(currentRepositoryModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Typing to find a repository...");
		getSettings().setFormatResult("gitop.choiceFormatter.comparableRepository.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.comparableRepository.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.comparableRepository.escapeMarkup");
	}

}
