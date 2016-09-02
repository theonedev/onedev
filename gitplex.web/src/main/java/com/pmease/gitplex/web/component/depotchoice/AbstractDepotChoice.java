package com.pmease.gitplex.web.component.depotchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.entity.Depot;

@SuppressWarnings("serial")
public class AbstractDepotChoice extends Select2Choice<Depot> {

	public AbstractDepotChoice(String id, IModel<Depot> depotModel, 
			AbstractDepotChoiceProvider choiceProvider) {
		super(id, depotModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a repository...");
		getSettings().setFormatResult("gitplex.depotChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.depotChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.depotChoiceFormatter.escapeMarkup");
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new DepotChoiceResourceReference()));
	}
	
}
