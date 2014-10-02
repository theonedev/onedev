package com.pmease.gitplex.core.model;

import javax.annotation.Nullable;

import org.apache.shiro.SecurityUtils;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.VoteEligibility;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.manager.VoteManager;
import com.pmease.gitplex.core.model.PullRequest.Status;
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
			return canVote(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(VoteManager.class).vote(request, user, Vote.Result.APPROVE, comment);
		}		
		
	},
	DISAPPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canVote(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(VoteManager.class).vote(request, user, Vote.Result.DISAPPROVE, comment);
		}
		
	};
	
	private static boolean canVote(PullRequest request) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		if (user == null)
			return false;

		if (request.getStatus() != Status.PENDING_APPROVAL)
			return false;
		
		if (GitPlex.getInstance(VoteManager.class).find(user, request.getLatestUpdate()) != null)
			return false;
		
		boolean canVote = false;
		for (VoteEligibility each: request.getCheckResult().getVoteEligibilities()) {
			if (each.canVote(user, request)) {
				canVote = true;
				break;
			}
		}
		return canVote;
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
}
