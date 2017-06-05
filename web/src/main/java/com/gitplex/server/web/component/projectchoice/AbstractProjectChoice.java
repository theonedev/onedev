package com.gitplex.server.web.component.projectchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class AbstractProjectChoice extends Select2Choice<ProjectFacade> {

	public AbstractProjectChoice(String id, IModel<ProjectFacade> projectModel, 
			AbstractProjectChoiceProvider choiceProvider) {
		super(id, projectModel, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose a project...");
		getSettings().setFormatResult("gitplex.server.projectChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.projectChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.projectChoiceFormatter.escapeMarkup");
	}

	@Override
	protected void onBeforeRender() {
		getSettings().setAllowClear(!isRequired());
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new ProjectChoiceResourceReference()));
	}
	
}
