package com.pmease.gitplex.web.component.user;

import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class RequestAssigneeChoice extends Select2Choice<RequestAssignee> {

	public RequestAssigneeChoice(String id, IModel<Repository> repoModel, final IModel<User> assigneeModel) {
		super(id, new IModel<RequestAssignee>() {

			@Override
			public void detach() {
			}

			@Override
			public RequestAssignee getObject() {
				User user = assigneeModel.getObject();
				if (user != null)
					return new RequestAssignee(user, null);
				else
					return null;
			}

			@Override
			public void setObject(RequestAssignee object) {
				if (object != null)
					assigneeModel.setObject(object.getUser());
				else
					assigneeModel.setObject(null);
			}
			
		}, new RequestAssigneeProvider(repoModel));
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
