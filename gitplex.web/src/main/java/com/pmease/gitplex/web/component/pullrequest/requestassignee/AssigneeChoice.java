package com.pmease.gitplex.web.component.pullrequest.requestassignee;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.Select2Choice;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.component.accountchoice.AccountChoiceResourceReference;
import com.pmease.gitplex.core.entity.Account;

@SuppressWarnings("serial")
public class AssigneeChoice extends Select2Choice<Assignee> {

	public AssigneeChoice(String id, IModel<Depot> depotModel, final IModel<Account> assigneeModel) {
		super(id, new IModel<Assignee>() {

			@Override
			public void detach() {
			}

			@Override
			public Assignee getObject() {
				Account user = assigneeModel.getObject();
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
			
		}, new AssigneeProvider(depotModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder("Type to find an assignee");
		getSettings().setFormatResult("gitplex.accountChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.accountChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.accountChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(AccountChoiceResourceReference.INSTANCE));
	}

}
