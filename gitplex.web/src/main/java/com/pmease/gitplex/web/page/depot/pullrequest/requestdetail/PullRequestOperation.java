package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.git.Git;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.entity.PullRequest.Status;
import com.pmease.gitplex.core.entity.component.IntegrationPreview;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;

public enum PullRequestOperation {
	INTEGRATE {

		@Override
		public boolean canOperate(PullRequest request) {
			if (!SecurityUtils.getSubject().isPermitted(
					ObjectPermission.ofDepotPush(request.getTargetDepot()))) {
				return false;
			} else {
				return GitPlex.getInstance(PullRequestManager.class).canIntegrate(request);
			}
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).integrate(request, comment);
		}
		
	},
	DISCARD {

		@Override
		public boolean canOperate(PullRequest request) {
			if (!SecurityUtils.canModify(request))
				return false;
			else 
				return request.isOpen();
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).discard(request, comment);
		}
		
	},
	APPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(ReviewManager.class).review(
					request, user, Review.Result.APPROVE, comment);
		}		
		
	},
	DISAPPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(ReviewManager.class).review(
					request, user, Review.Result.DISAPPROVE, comment);
		}
		
	},
	REOPEN {

		@Override
		public boolean canOperate(PullRequest request) {
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			if (request.isOpen() 
					|| !SecurityUtils.canModify(request)
					|| request.getTarget().getObjectName(false) == null
					|| request.getSourceDepot() == null 
					|| request.getSource().getObjectName(false) == null
					|| pullRequestManager.findOpen(request.getTarget(), request.getSource()) != null) {
				return false;
			}
			
			// now check if source branch is integrated into target branch
			Git git = request.getTargetDepot().git();
			String sourceHead = request.getSource().getObjectName();
			return git.parseRevision(sourceHead, false) == null 
					|| !request.getTargetDepot().isAncestor(sourceHead, request.getTarget().getObjectName());
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).reopen(request, comment);
		}
		
	},
	DELETE_SOURCE_BRANCH {

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).deleteSourceBranch(request);
		}

		@Override
		public boolean canOperate(PullRequest request) {
			IntegrationPreview preview = request.getLastIntegrationPreview();
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			return request.getStatus() == Status.INTEGRATED 
					&& request.getSourceDepot() != null		
					&& request.getSource().getObjectName(false) != null
					&& !request.getSource().isDefault()
					&& preview != null
					&& (request.getSource().getObjectName().equals(preview.getRequestHead()) 
							|| request.getSource().getObjectName().equals(preview.getIntegrated()))
					&& SecurityUtils.canModify(request)
					&& SecurityUtils.canPushRef(request.getSourceDepot(), request.getSourceRef(), request.getSource().getObjectId(), ObjectId.zeroId())
					&& pullRequestManager.queryOpenTo(request.getSource(), null).isEmpty();
		}
		
	}, 
	RESTORE_SOURCE_BRANCH {

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).restoreSourceBranch(request);
		}

		@Override
		public boolean canOperate(PullRequest request) {
			return request.getSourceDepot() != null 
					&& request.getSource().getObjectName(false) == null 
					&& SecurityUtils.canModify(request) 
					&& SecurityUtils.canPushRef(request.getSourceDepot(), request.getSourceRef(), ObjectId.zeroId(), ObjectId.fromString(request.getLatestUpdate().getHeadCommitHash()));
		}
		
	};
	
	private static boolean canReview(PullRequest request) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		
		// call request.getStatus() in order to trigger generation of review
		// integrations which will be used in else condition 
		if (user == null  
				|| request.getStatus() == PullRequest.Status.INTEGRATED 
				|| request.getStatus() == PullRequest.Status.DISCARDED
				|| request.isReviewEffective(user)) { 
			return false;
		} else {
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (invitation.isPreferred() && invitation.getReviewer().equals(user))
					return true;
			}
			return false;
		}
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
}
