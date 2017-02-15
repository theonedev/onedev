package com.gitplex.server.web.component.branchchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.web.component.select2.ChoiceProvider;
import com.gitplex.server.web.component.select2.Select2MultiChoice;

@SuppressWarnings("serial")
public class BranchMultiChoice extends Select2MultiChoice<String> {

	public BranchMultiChoice(String id, IModel<Collection<String>> model, ChoiceProvider<String> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose branches ...");
		getSettings().setFormatResult("gitplex.server.branchChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.branchChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.branchChoiceFormatter.escapeMarkup");
		
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BranchChoiceResourceReference()));
	}
	
}
