package com.pmease.gitop.web.component.choice;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.core.model.User;
import com.vaynberg.wicket.select2.Select2MultiChoice;

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
		getSettings().setFormatResult("UserChoice.formatter.formatResult");
		getSettings()
				.setFormatSelection("UserChoice.formatter.formatSelection");
		getSettings().setEscapeMarkup("UserChoice.formatter.escapeMarkup");
	}

	private ResourceReference userChoiceReference = new PackageResourceReference(
			UserSingleChoice.class, "userchoice.js");

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(userChoiceReference));
	}
}