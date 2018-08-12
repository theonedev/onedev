package io.onedev.server.web.component.projectchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.web.component.select2.Select2Choice;

@SuppressWarnings("serial")
public class ProjectSingleChoice extends Select2Choice<ProjectFacade> {

	public ProjectSingleChoice(String id, IModel<ProjectFacade> model, AbstractProjectChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose a project ...");
		getSettings().setFormatResult("onedev.server.projectChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.projectChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.projectChoiceFormatter.escapeMarkup");
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