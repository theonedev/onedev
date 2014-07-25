package com.pmease.gitplex.core.pullrequest;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.GitPlex;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import com.pmease.gitplex.core.gatekeeper.voteeligibility.VoteEligibility;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.manager.VoteManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Vote;
import com.pmease.gitplex.core.model.PullRequest.Status;
import com.pmease.gitplex.core.permission.ObjectPermission;

public enum RequestOperation {
	INTEGRATE {

		@Override
		public void checkOperate(PullRequest request) {
			if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryWrite(request.getTarget().getRepository())))
				throw new UnauthorizedException();
			if (request.getStatus() != Status.PENDING_INTEGRATE)
				throw new RequestOperateException("Request is not pending integrate.");
			if (request.getIntegrationInfo() == null)
				throw new RequestOperateException("Request is not refreshed yet.");
			if (request.getIntegrationInfo().getIntegrationHead() == null)
				throw new RequestOperateException("There are integrate conflicts.");
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			if (!GitPlex.getInstance(PullRequestManager.class).integrate(request, user, comment)) {
				throw new RequestOperateException("Unable to update relevant branches due to lock failure, "
						+ "please try again later.");
			}
		}
		
	},
	DISCARD {

		@Override
		public void checkOperate(PullRequest request) {
			if (!GitPlex.getInstance(AuthorizationManager.class).canModify(request))
				throw new UnauthorizedException();
			if (!request.isOpen())
				throw new RequestOperateException("Request is already closed.");
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(PullRequestManager.class).discard(request, user, comment);
		}
		
	},
	APPROVE {

		@Override
		public void checkOperate(PullRequest request) {
			checkVote(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(VoteManager.class).vote(request, user, Vote.Result.APPROVE, comment);
		}		
		
	},
	DISAPPROVE {

		@Override
		public void checkOperate(PullRequest request) {
			checkVote(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = GitPlex.getInstance(UserManager.class).getCurrent();
			GitPlex.getInstance(VoteManager.class).vote(request, user, Vote.Result.DISAPPROVE, comment);
		}
		
	};
	
	private static void checkVote(PullRequest request) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		if (user == null)
			throw new UnauthorizedException();

		if (request.getStatus() != Status.PENDING_APPROVAL)
			throw new RequestOperateException("Request is not pending approval.");
		
		if (GitPlex.getInstance(VoteManager.class).find(user, request.getLatestUpdate()) != null)
			throw new RequestOperateException("Current user has already voted for latest update of request.");
		
		boolean canVote = false;
		for (VoteEligibility each: request.getCheckResult().getVoteEligibilities()) {
			if (each.canVote(user, request)) {
				canVote = true;
				break;
			}
		}
		if (!canVote)
			throw new RequestOperateException("Current user is not eligible to vote the request");
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract void checkOperate(PullRequest request);
	
	public boolean canOperate(PullRequest request) {
		try {
			checkOperate(request);
			return true;
		} catch (UnauthorizedException | RequestOperateException e) {
			return false;
		}
	}
	
}
