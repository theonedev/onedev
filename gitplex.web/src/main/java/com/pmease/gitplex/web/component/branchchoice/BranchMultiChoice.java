package com.pmease.gitplex.web.component.branchchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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
		getSettings().setFormatResult("gitplex.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.branchChoiceFormatter.escapeMarkup");
		
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(BranchChoiceResourceReference.INSTANCE));
	}
	
}
