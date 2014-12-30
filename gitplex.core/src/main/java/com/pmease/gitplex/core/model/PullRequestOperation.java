package com.pmease.gitplex.core.model;

import javax.annotation.Nullable;

import org.apache.shiro.SecurityUtils;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.permission.ObjectPermission;

public enum PullRequestOperation {
	INTEGRATE {

		@Override
		public boolean canOperate(PullRequest request) {
			if (!SecurityUtils.getSubject().isPermitted(
					ObjectPermission.ofRepositoryWrite(request.getTarget().getRepository()))) {
				return false;
			} else {
				return GitPlex.getInstance(PullRequestManager.class).canIntegrate(request);
			}
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(PullRequestManager.class).integrate(request, user, comment);
		}
		
	},
	DISCARD {

		@Override
		public boolean canOperate(PullRequest request) {
			if (!GitPlex.getInstance(AuthorizationManager.class).canModify(request))
				return false;
			else 
				return request.isOpen();
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(PullRequestManager.class).discard(request, user, comment);
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
		
	};
	
	private static boolean canReview(PullRequest request) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		if (user == null 
				|| user.equals(request.getSubmitter()) 
				|| !request.isOpen() 
				|| request.isReviewEffective(user)) { 
			return false;
		} else {
			for (ReviewInvitation invitation: request.getReviewInvitations()) {
				if (!invitation.isExcluded() && invitation.getReviewer().equals(user))
					return true;
			}
			return false;
		}
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
}
