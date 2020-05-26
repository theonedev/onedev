package io.onedev.server.web.component.pullrequest.assignment;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.user.choice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public abstract class AssigneeChoice extends SelectToAddChoice<User> {

	public AssigneeChoice(String id) {
		super(id);
		
		setProvider(new AssigneeProvider() {

			@Override
			protected PullRequest getPullRequest() {
				return AssigneeChoice.this.getPullRequest();
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Add assignee...");
		getSettings().setFormatResult("onedev.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, User user) {
		PullRequestAssignment assignment = new PullRequestAssignment();
		assignment.setRequest(getPullRequest());
		assignment.setUser(user);

		if (!getPullRequest().isNew())
			OneDev.getInstance(PullRequestAssignmentManager.class).addAssignee(assignment);
		else
			getPullRequest().getAssignments().add(assignment);
	};
	
	protected abstract PullRequest getPullRequest();
}
