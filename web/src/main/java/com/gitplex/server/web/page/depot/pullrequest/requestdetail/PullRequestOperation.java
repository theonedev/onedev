package com.gitplex.server.web.page.depot.pullrequest.requestdetail;

import javax.annotation.Nullable;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.MergePreview;
import com.gitplex.server.security.SecurityUtils;

public enum PullRequestOperation {
	DISCARD {

		@Override
		public boolean canOperate(PullRequest request) {
			return request.isOpen() && SecurityUtils.canModify(request);
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
			GitPlex.getInstance(ReviewManager.class).review(request, true, comment);
		}

	},
	DISAPPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(ReviewManager.class).review(request, false, comment);
		}

	},
	REOPEN {

		@Override
		public boolean canOperate(PullRequest request) {
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			return !request.isOpen() 
					&& SecurityUtils.canModify(request)
					&& request.getTarget().getObjectName(false) != null
					&& request.getSourceDepot() != null 
					&& request.getSource().getObjectName(false) != null
					&& pullRequestManager.findEffective(request.getTarget(), request.getSource()) == null
					&& !GitUtils.isMergedInto(request.getTargetDepot().getRepository(), 
							request.getSource().getObjectId(), request.getTarget().getObjectId());
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).reopen(request, comment);
		}

	},
	DELETE_SOURCE_BRANCH {

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).deleteSourceBranch(request, comment);
		}

		@Override
		public boolean canOperate(PullRequest request) {
			MergePreview preview = request.getLastMergePreview();
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			return request.isMerged()
					&& request.getSourceDepot() != null		
					&& request.getSource().getObjectName(false) != null
					&& !request.getSource().isDefault()
					&& preview != null
					&& (request.getSource().getObjectName().equals(preview.getRequestHead()) 
							|| request.getSource().getObjectName().equals(preview.getMerged()))
					&& SecurityUtils.canModify(request)
					&& SecurityUtils.canDeleteBranch(request.getSourceDepot(), request.getSourceBranch())
					&& pullRequestManager.findAllOpenTo(request.getSource()).isEmpty();
		}

	}, 
	RESTORE_SOURCE_BRANCH {

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).restoreSourceBranch(request, comment);
		}

		@Override
		public boolean canOperate(PullRequest request) {
			return request.getSourceDepot() != null 
					&& request.getSource().getObjectName(false) == null 
					&& SecurityUtils.canModify(request) 
					&& SecurityUtils.canWrite(request.getSourceDepot());
		}

	};
	
	private static boolean canReview(PullRequest request) {
		Account user = GitPlex.getInstance(AccountManager.class).getCurrent();
		
		return request.getReviewCheckStatus().getAwaitingReviewers().contains(user);
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
	
}
