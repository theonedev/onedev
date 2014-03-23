package com.pmease.gitop.web.component.choice;

import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.Repository;
import com.vaynberg.wicket.select2.Select2Choice;

@SuppressWarnings("serial")
public class ComparableProjectChoice extends Select2Choice<Repository> {

	/**
	 * Constructor with model.
	 * 
	 * @param id
	 * 			component id of the choice
	 * @param currentProjectModel
	 * 			model of current project from which to calculate comparable projects. Note that 
	 * 			model.getObject() should never return null
	 * @param selectedProjectModel
	 * 			model of selected project
	 */
	public ComparableProjectChoice(String id, IModel<Repository> currentProjectModel, IModel<Repository> selectedProjectModel) {
		super(id, selectedProjectModel, new ComparableProjectChoiceProvider(currentProjectModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Typing to find a project...");
		getSettings().setFormatResult("gitop.choiceFormatter.comparableProject.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.comparableProject.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.comparableProject.escapeMarkup");
	}

}
