package com.pmease.gitplex.web.component.repochoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class AffinalRepositoryChoice extends Select2Choice<Repository> {

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
	public AffinalRepositoryChoice(String id, IModel<Repository> currentRepositoryModel, IModel<Repository> selectedRepositoryModel) {
		super(id, selectedRepositoryModel, new AffinalRepositoryChoiceProvider(currentRepositoryModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Typing to find a repository...");
		getSettings().setFormatResult("gitplex.repoChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.repoChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.repoChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(RepoChoiceResourceReference.INSTANCE));
	}

}
