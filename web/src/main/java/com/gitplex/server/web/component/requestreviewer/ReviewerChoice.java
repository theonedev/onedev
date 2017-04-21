package com.gitplex.server.web.component.requestreviewer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ReviewInvitationManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.web.component.accountchoice.AccountChoiceResourceReference;
import com.gitplex.server.web.component.select2.SelectToAddChoice;

@SuppressWarnings("serial")
public abstract class ReviewerChoice extends SelectToAddChoice<Account> {

	private final IModel<PullRequest> requestModel;
	
	public ReviewerChoice(String id, IModel<PullRequest> requestModel) {
		super(id, new ReviewerProvider(requestModel));
		
		this.requestModel = requestModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setPlaceholder("Select user to add as reviewer...");
		getSettings().setFormatResult("gitplex.server.accountChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.server.accountChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.server.accountChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new AccountChoiceResourceReference()));
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Account user) {
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

		request.clearReviewCheckStatus();
		
		if (!request.isNew())
			GitPlex.getInstance(ReviewInvitationManager.class).invite(invitation);
	};
	
}
