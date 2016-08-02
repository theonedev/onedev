package com.pmease.gitplex.web.component.pullrequest.requestreviewer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.RemoveableAvatar;
import com.pmease.gitplex.web.model.EntityModel;
import com.pmease.gitplex.web.model.UserModel;

@SuppressWarnings("serial")
public class ReviewerAvatar extends RemoveableAvatar {

	private final ReviewInvitation invitation;
	
	private final IModel<PullRequest> requestModel;
	
	public ReviewerAvatar(String id, ReviewInvitation invitation) {
		super(id, new UserModel(invitation.getUser()), "Remove reviewer");
		
		this.invitation = invitation;
		requestModel = new EntityModel<PullRequest>(invitation.getRequest());		
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		PullRequest request = requestModel.getObject();
		Account currentUser = GitPlex.getInstance(AccountManager.class).getCurrent();
		
		boolean reviewEffective = false;
		if (!request.isNew())
			reviewEffective = request.isReviewEffective(getUser());
		setEnabled(request.isOpen() && !reviewEffective 
				&& (currentUser!= null && currentUser.equals(request.getSubmitter()) || SecurityUtils.canManage(request.getTargetDepot())));
	}

	@Override
	protected void onAvatarRemove(AjaxRequestTarget target) {
		Date now = new Date();
		PullRequest request = requestModel.getObject();
		Set<Account> prevInvited = new HashSet<>();
		for (ReviewInvitation each: request.getReviewInvitations()) {
			if (each.isPreferred())
				prevInvited.add(each.getUser());
			if (each.getUser().equals(invitation.getUser())) {
				each.setPerferred(false);
				each.setDate(new Date());
			}
		}
		request.getTargetDepot().getGateKeeper().checkRequest(request);

		Set<Account> nowInvited = new HashSet<>();
		for (ReviewInvitation each: request.getReviewInvitations()) {
			if (each.isPreferred())
				nowInvited.add(each.getUser());
		}
		
		if (nowInvited.contains(invitation.getUser())) {
			getSession().warn("Reviewer '" + invitation.getUser().getDisplayName() 
					+ "' is required by gate keeper and can not be removed");
		} else {
			nowInvited.removeAll(prevInvited);
			if (!nowInvited.isEmpty()) {
				getSession().warn("Reviewer '" + invitation.getUser().getDisplayName() 
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
