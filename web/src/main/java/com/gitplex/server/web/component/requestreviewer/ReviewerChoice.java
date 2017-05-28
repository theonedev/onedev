package com.gitplex.server.web.component.requestreviewer;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ReviewInvitationManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.web.component.userchoice.UserChoiceResourceReference;
import com.gitplex.server.web.component.select2.SelectToAddChoice;

@SuppressWarnings("serial")
public abstract class ReviewerChoice extends SelectToAddChoice<User> {

	private final IModel<PullRequest> requestModel;
	
	public ReviewerChoice(String id, IModel<PullRequest> requestModel) {
		super(id, new ReviewerProvider(requestModel));
		
		this.requestModel = requestModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Select user to add as reviewer...");
		getSettings().setFormatResult("gitplex.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.userChoiceFormatter.escapeMarkup");
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
		ReviewInvitation invitation = null;
		for(ReviewInvitation each: request.getReviewInvitations()) {
			if (each.getUser().equals(user)) {
				invitation = each;
				break;
			}
		}
		if (invitation == null) {
			invitation = new ReviewInvitation();
			invitation.setRequest(request);
			invitation.setUser(user);
			request.getReviewInvitations().add(invitation);
		}
		invitation.setType(ReviewInvitation.Type.MANUAL);
		invitation.setDate(new Date());

		request.clearReviewStatus();
		
		if (!request.isNew())
			GitPlex.getInstance(ReviewInvitationManager.class).invite(invitation);
	};
	
}
