package com.pmease.gitplex.web.component.userchoice;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.assets.userchoice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public class UserSingleChoice extends Select2Choice<User> {

	private final boolean allowEmpty;
	
	public UserSingleChoice(String id, IModel<User> model, boolean allowEmpty) {
		super(id, model, new UserChoiceProvider());
		
		this.allowEmpty = allowEmpty;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setAllowClear(allowEmpty);
		getSettings().setPlaceholder("Typing to find an user ...");
		getSettings().setFormatResult("gitplex.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(UserChoiceResourceReference.INSTANCE));
	}
	
}
