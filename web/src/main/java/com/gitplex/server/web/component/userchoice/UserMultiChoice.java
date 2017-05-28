package com.gitplex.server.web.component.userchoice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.User;
import com.gitplex.server.web.component.userchoice.UserChoiceResourceReference;
import com.gitplex.server.web.component.select2.Select2MultiChoice;

public class UserMultiChoice extends Select2MultiChoice<User> {

	private static final long serialVersionUID = 1L;

	public UserMultiChoice(String id, IModel<Collection<User>> model, 
			AbstractUserChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose users ...");
		getSettings().setFormatResult("gitplex.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}

}
