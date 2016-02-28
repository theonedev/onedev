package com.pmease.gitplex.web.component.pullrequest.requestreviewer;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.assets.userchoice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public abstract class ReviewerChoice extends SelectToAddChoice<Account> {

	private static final String PLACEHOLDER = "Select user to add as reviewer...";
	
	private final IModel<PullRequest> requestModel;
	
	public ReviewerChoice(String id, IModel<PullRequest> requestModel) {
		super(id, new ReviewerProvider(requestModel), PLACEHOLDER);
		
		this.requestModel = requestModel;
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		PullRequest request = requestModel.getObject();
		Account currentUser = GitPlex.getInstance(AccountManager.class).getCurrent();
		setVisible(request.isOpen() 
				&& !request.getPotentialReviewers().isEmpty()
				&& (currentUser != null && currentUser.equals(request.getSubmitter()) || SecurityUtils.canManage(request.getTargetDepot())));
	}
                                                                                                                              
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		// getSettings().setMinimumInputLength(1);
		getSettings().setPlaceholder(PLACEHOLDER);
		getSettings().setFormatResult("gitplex.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("gitplex.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("gitplex.userChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(UserChoiceResourceReference.INSTANCE));
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

	protected void onSelect(AjaxRequestTarget target, Account user) {
		PullRequest request = requestModel.getObject();
		ReviewInvitation invitation = null;
		for(ReviewInvitation each: request.getReviewInvitations()) {
			if (each.getReviewer().equals(user)) {
				invitation = each;
				break;
			}
		}
		if (invitation != null) {
			invitation.setPerferred(true);
			invitation.setDate(new Date());
		} else {
			invitation = new ReviewInvitation();
			invitation.setRequest(request);
			invitation.setReviewer(user);
			request.getReviewInvitations().add(invitation);
		}
		if (!request.isNew())
			GitPlex.getInstance(ReviewInvitationManager.class).save(invitation);
	};
}
