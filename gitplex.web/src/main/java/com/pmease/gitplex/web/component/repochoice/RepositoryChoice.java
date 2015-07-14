package com.pmease.gitplex.web.component.repochoice;

import javax.annotation.Nullable;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class RepositoryChoice extends Select2Choice<Repository> {

	private final boolean allowEmpty;
	
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
	public RepositoryChoice(String id, IModel<Repository> repoModel, @Nullable IModel<User> userModel, boolean allowEmpty) {
		super(id, repoModel, new RepositoryChoiceProvider(userModel));
		
		this.allowEmpty = allowEmpty;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(allowEmpty);
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
