package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2MultiChoice;
import com.pmease.gitop.model.User;

public class UserMultiChoice extends Select2MultiChoice<User> {

	private static final long serialVersionUID = 1L;

	public UserMultiChoice(String id, IModel<Collection<User>> model) {
		super(id, model, new UserChoiceProvider());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Start typing to find users ...");
		getSettings().setFormatResult("gitop.choiceFormatter.user.formatResult");
		getSettings().setFormatSelection("gitop.choiceFormatter.user.formatSelection");
		getSettings().setEscapeMarkup("gitop.choiceFormatter.user.escapeMarkup");
	}

}