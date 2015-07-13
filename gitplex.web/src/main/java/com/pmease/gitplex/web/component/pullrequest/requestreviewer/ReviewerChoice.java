package com.pmease.gitplex.web.component.pullrequest.requestreviewer;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.select2.SelectToAddChoice;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.assets.userchoice.UserChoiceResourceReference;

@SuppressWarnings("serial")
public abstract class ReviewerChoice extends SelectToAddChoice<User> {

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
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		setVisible(request.isOpen() 
				&& !request.getPotentialReviewers().isEmpty()
				&& (request.getSubmitter().equals(currentUser) || SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepoAdmin(request.getTargetRepo()))));
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

	protected void onSelect(AjaxRequestTarget target, User user) {
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
