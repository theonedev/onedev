package com.pmease.gitplex.web.component.pullrequest;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class AssigneeChoice extends Select2Choice<Assignee> {

	public AssigneeChoice(String id, IModel<Repository> repoModel, final IModel<User> assigneeModel) {
		super(id, new IModel<Assignee>() {

			@Override
			public void detach() {
			}

			@Override
			public Assignee getObject() {
				User user = assigneeModel.getObject();
				if (user != null)
					return new Assignee(user, null);
				else
					return null;
			}

			@Override
			public void setObject(Assignee object) {
				if (object != null)
					assigneeModel.setObject(object.getUser());
				else
					assigneeModel.setObject(null);
			}
			
		}, new AssigneeProvider(repoModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Typing to find an assignee ...");
		getSettings().setFormatResult("gitplex.choiceFormatter.user.formatResult");
		getSettings().setFormatSelection("gitplex.choiceFormatter.user.formatSelection");
		getSettings().setEscapeMarkup("gitplex.choiceFormatter.user.escapeMarkup");
	}

}
