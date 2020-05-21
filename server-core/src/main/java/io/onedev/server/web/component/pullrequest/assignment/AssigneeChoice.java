package io.onedev.server.web.component.pullrequest.assignment;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.User;
import io.onedev.server.web.component.select2.SelectToAddChoice;
import io.onedev.server.web.component.user.choice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public class AssigneeChoice extends SelectToAddChoice<User> {

	private final IModel<PullRequest> requestModel;
	
	public AssigneeChoice(String id, IModel<PullRequest> requestModel) {
		super(id, new AssigneeProvider(requestModel));
		
		this.requestModel = requestModel;
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
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, User user) {
		PullRequest request = requestModel.getObject();
		PullRequestAssignment assignment = new PullRequestAssignment();
		assignment.setRequest(request);
		assignment.setUser(user);

		if (!request.isNew())
			OneDev.getInstance(PullRequestAssignmentManager.class).addAssignee(assignment);
		else
			request.getAssignments().add(assignment);
	};
	
}
