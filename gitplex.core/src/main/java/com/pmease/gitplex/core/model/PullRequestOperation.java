package com.pmease.gitplex.core.model;

import javax.annotation.Nullable;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.git.Git;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.permission.Permission;

public enum PullRequestOperation {
	INTEGRATE {

		@Override
		public boolean canOperate(PullRequest request) {
			if (!SecurityUtils.getSubject().isPermitted(
					Permission.ofRepositoryWrite(request.getTarget().getRepository()))) {
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
			if (!GitPlex.getInstance(AuthorizationManager.class).canModifyRequest(request))
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
			AuthorizationManager authorizationManager = GitPlex.getInstance(AuthorizationManager.class);
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			if (request.isOpen() 
					|| !authorizationManager.canModifyRequest(request)
					|| request.getSource() == null 
					|| pullRequestManager.findOpen(request.getTarget(), request.getSource()) != null) {
				return false;
			}
			
			// now check if source branch is integrated into target branch
			Git git = request.getTarget().getRepository().git();
			String sourceHead = request.getSource().getHeadCommitHash();
			return git.parseRevision(sourceHead, false) == null 
					|| !git.isAncestor(sourceHead, request.getTarget().getHeadCommitHash());
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).reopen(request, comment);
		}
		
	};
	
	private static boolean canReview(PullRequest request) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		if (user == null  
				|| !request.isOpen() 
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
