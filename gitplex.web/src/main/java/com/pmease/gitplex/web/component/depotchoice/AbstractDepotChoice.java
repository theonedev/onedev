package com.pmease.gitplex.web.component.depotchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.entity.Depot;

@SuppressWarnings("serial")
public class AbstractDepotChoice extends Select2Choice<Depot> {

	private final boolean allowEmpty;
	
	public AbstractDepotChoice(String id, IModel<Depot> depotModel, 
			AbstractDepotChoiceProvider choiceProvider, boolean allowEmpty) {
		super(id, depotModel, choiceProvider);
		
		this.allowEmpty = allowEmpty;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(allowEmpty);
		getSettings().setPlaceholder("Choose a repository...");
		getSettings().setFormatResult("gitplex.depotChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.depotChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.depotChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(DepotChoiceResourceReference.INSTANCE));
	}
	
}
