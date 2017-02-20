package com.gitplex.server.web.component.depotchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.select2.Select2Choice;

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
		getSettings().setFormatResult("gitplex.server.depotChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.depotChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.depotChoiceFormatter.escapeMarkup");
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
