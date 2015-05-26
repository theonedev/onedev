package com.pmease.gitplex.web.component.repochoice;

import javax.annotation.Nullable;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class RepositoryChoice extends Select2Choice<Repository> {

	/**
	 * Constructor with model.
	 * 
	 * @param id
	 * 			component id of the choice
	 * @param repoModel
	 * 			repository model
	 * @param userModel
	 * 			model of user to choose repository under, <tt>null</tt> to choose all accessible repositories
	 */
	public RepositoryChoice(String id, IModel<Repository> repoModel, @Nullable IModel<User> userModel) {
		super(id, repoModel, new RepositoryChoiceProvider(userModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Typing to find a repository...");
		getSettings().setFormatResult("gitplex.choiceFormatter.repository.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.repository.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.repository.escapeMarkup");
	}

}
