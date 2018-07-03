package io.onedev.server.web.page.project.pullrequests.requestdetail;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestReviewManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.security.SecurityUtils;

public enum PullRequestOperation {
	DISCARD {

		@Override
		public boolean canOperate(PullRequest request) {
			return request.isOpen() && SecurityUtils.canModify(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			OneDev.getInstance(PullRequestManager.class).discard(request, comment);
		}

	},
	APPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = SecurityUtils.getUser();
			PullRequestReview review = request.getReview(user);
			Preconditions.checkState(review != null);
			ReviewResult result = new ReviewResult();
			result.setApproved(true);
			result.setComment(comment);
			result.setCommit(request.getHeadCommitHash());
			review.setResult(result);
			OneDev.getInstance(PullRequestReviewManager.class).review(review);
		}

	},
	REQUEST_FOR_CHANGES {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			User user = SecurityUtils.getUser();
			PullRequestReview review = request.getReview(user);
			Preconditions.checkState(review != null);
			ReviewResult result = new ReviewResult();
			result.setApproved(false);
			result.setComment(comment);
			result.setCommit(request.getHeadCommitHash());
			review.setResult(result);
			OneDev.getInstance(PullRequestReviewManager.class).review(review);
		}

	},
	REOPEN {

		@Override
		public boolean canOperate(PullRequest request) {
			PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
			return !request.isOpen() 
					&& SecurityUtils.canModify(request)
					&& request.getTarget().getObjectName(false) != null
					&& request.getSourceProject() != null 
					&& request.getSource().getObjectName(false) != null
					&& pullRequestManager.findEffective(request.getTarget(), request.getSource()) == null
					&& !GitUtils.isMergedInto(request.getTargetProject().getRepository(), null,
							request.getSource().getObjectId(), request.getTarget().getObjectId());
		}

		@Override
		public void operate(PullRequest request, String comment) {
			OneDev.getInstance(PullRequestManager.class).reopen(request, comment);
		}

	},
	DELETE_SOURCE_BRANCH {

		@Override
		public void operate(PullRequest request, String comment) {
			OneDev.getInstance(PullRequestManager.class).deleteSourceBranch(request, comment);
		}

		@Override
		public boolean canOperate(PullRequest request) {
			MergePreview preview = request.getLastMergePreview();
			PullRequestManager pullRequestManager = OneDev.getInstance(PullRequestManager.class);
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
			OneDev.getInstance(PullRequestManager.class).restoreSourceBranch(request, comment);
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
		User user = OneDev.getInstance(UserManager.class).getCurrent();
		PullRequestReview review = request.getReview(user);
		return review != null && review.getExcludeDate() == null;
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
	
}
