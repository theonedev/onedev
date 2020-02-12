package io.onedev.server.web.component.project.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.select2.Select2MultiChoice;

public class ProjectMultiChoice extends Select2MultiChoice<Project> {

	private static final long serialVersionUID = 1L;

	public ProjectMultiChoice(String id, IModel<Collection<Project>> model, AbstractProjectChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Choose projects...");
		else
			getSettings().setPlaceholder("Not specified");
		getSettings().setFormatResult("onedev.server.projectChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.projectChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.projectChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new ProjectChoiceResourceReference()));
	}

}