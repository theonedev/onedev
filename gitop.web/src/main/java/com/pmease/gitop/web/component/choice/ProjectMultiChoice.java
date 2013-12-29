package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Project;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2MultiChoice;

public class ProjectMultiChoice extends Select2MultiChoice<Project> {
	private static final long serialVersionUID = 1L;

	public ProjectMultiChoice(String id, IModel<Collection<Project>> model, ChoiceProvider<Project> provider) {
		super(id, model, provider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Start typing to find projects ...");
		getSettings().setFormatResult("gitop.choiceFormatter.project.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.project.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.project.escapeMarkup");
	}

}
