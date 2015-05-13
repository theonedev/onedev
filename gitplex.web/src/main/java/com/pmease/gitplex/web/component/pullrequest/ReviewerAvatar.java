package com.pmease.gitplex.web.component.pullrequest;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.component.avatar.RemoveableAvatar;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class ReviewerAvatar extends RemoveableAvatar {

	private final ReviewInvitation invitation;
	
	private final IModel<PullRequest> requestModel;
	
	public ReviewerAvatar(String id, ReviewInvitation invitation) {
		super(id, new UserModel(invitation.getReviewer()), "Remove reviewer");
		
		this.invitation = invitation;
		requestModel = new EntityModel<PullRequest>(invitation.getRequest());		
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		PullRequest request = requestModel.getObject();
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		
		boolean reviewEffective = false;
		if (!request.isNew())
			reviewEffective = request.isReviewEffective(getUser());
		setEnabled(request.isOpen() && !reviewEffective 
				&& (request.getSubmitter().equals(currentUser) || SecurityUtils.getSubject().isPermitted(Permission.ofRepositoryAdmin(request.getTarget().getRepository()))));
	}

	@Override
	protected void onAvatarRemove(AjaxRequestTarget target) {
		Date now = new Date();
		PullRequest request = requestModel.getObject();
		Set<User> prevInvited = new HashSet<>();
		for (ReviewInvitation each: request.getReviewInvitations()) {
			if (each.isPreferred())
				prevInvited.add(each.getReviewer());
			if (each.getReviewer().equals(invitation.getReviewer())) {
				each.setPerferred(false);
				each.setDate(new Date());
			}
		}
		request.getTarget().getRepository().getGateKeeper().checkRequest(request);

		Set<User> nowInvited = new HashSet<>();
		for (ReviewInvitation each: request.getReviewInvitations()) {
			if (each.isPreferred())
				nowInvited.add(each.getReviewer());
		}
		
		if (nowInvited.contains(invitation.getReviewer())) {
			getSession().warn("Reviewer '" + invitation.getReviewer().getDisplayName() 
					+ "' is required by gate keeper and can not be removed");
		} else {
			nowInvited.removeAll(prevInvited);
			if (!nowInvited.isEmpty()) {
				getSession().warn("Reviewer '" + invitation.getReviewer().getDisplayName() 
						+ "' is removed and user '" + nowInvited.iterator().next().getDisplayName() 
						+ "' is added as reviewer automatically to satisfy gate keeper requirement.");
			}
		}
		if (!request.isNew()) {
			ReviewInvitationManager reviewInvitationManager = GitPlex.getInstance(ReviewInvitationManager.class);
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (!invitation.getDate().before(now))
					reviewInvitationManager.save(invitation);
			}
		}
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		
		super.onDetach();
	}

}
