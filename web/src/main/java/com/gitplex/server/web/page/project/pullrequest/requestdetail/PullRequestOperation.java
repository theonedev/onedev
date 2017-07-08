package com.gitplex.server.web.page.project.pullrequest.requestdetail;

import javax.annotation.Nullable;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.Review;
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
			Review review = new Review();
			review.setApproved(true);
			review.setCommit(request.getHeadCommitHash());
			review.setNote(comment);
			review.setRequest(request);
			review.setUser(SecurityUtils.getUser());
			GitPlex.getInstance(ReviewManager.class).save(review);
		}

	},
	DISAPPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			Review review = new Review();
			review.setApproved(false);
			review.setCommit(request.getHeadCommitHash());
			review.setNote(comment);
			review.setRequest(request);
			review.setUser(SecurityUtils.getUser());
			GitPlex.getInstance(ReviewManager.class).save(review);
		}

	},
	REOPEN {

		@Override
		public boolean canOperate(PullRequest request) {
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			return !request.isOpen() 
					&& SecurityUtils.canModify(request)
					&& request.getTarget().getObjectName(false) != null
					&& request.getSourceProject() != null 
					&& request.getSource().getObjectName(false) != null
					&& pullRequestManager.findEffective(request.getTarget(), request.getSource()) == null
					&& !GitUtils.isMergedInto(request.getTargetProject().getRepository(), 
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
					&& request.getSourceProject() != null		
					&& request.getSource().getObjectName(false) != null
					&& !request.getSource().isDefault()
					&& preview != null
					&& (request.getSource().getObjectName().equals(preview.getRequestHead()) 
							|| request.getSource().getObjectName().equals(preview.getMerged()))
					&& SecurityUtils.canModify(request)
					&& SecurityUtils.canDeleteBranch(request.getSourceProject(), request.getSourceBranch())
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
			return request.getSourceProject() != null 
					&& request.getSource().getObjectName(false) == null 
					&& SecurityUtils.canModify(request) 
					&& SecurityUtils.canWrite(request.getSourceProject());
		}

	};
	
	private static boolean canReview(PullRequest request) {
		User user = GitPlex.getInstance(UserManager.class).getCurrent();
		
		return request.getQualityCheckStatus().getAwaitingReviewers().contains(user);
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
	
}
