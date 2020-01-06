package io.onedev.server.web.page.project.pullrequests.detail;

import javax.annotation.Nullable;

import org.apache.wicket.Session;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.ReviewResult;
import io.onedev.server.util.SecurityUtils;

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
			if (request.isOpen()) {
				PullRequestReview review = request.getReview(SecurityUtils.getUser());
				return review != null && review.getExcludeDate() == null 
						&& (review.getResult() == null || !review.getResult().isApproved());
			} else {
				return false;
			}
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
			Session.get().success("Approved");
		}

	},
	REQUEST_FOR_CHANGES {

		@Override
		public boolean canOperate(PullRequest request) {
			if (request.isOpen()) {
				PullRequestReview review = request.getReview(SecurityUtils.getUser());
				return review != null && review.getExcludeDate() == null 
						&& (review.getResult() == null || review.getResult().isApproved());
			} else {
				return false;
			}
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
			Session.get().success("Requested for changes");
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
			Session.get().success("Deleted source branch");
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
					&& pullRequestManager.queryOpenTo(request.getSource()).isEmpty();
		}

	}, 
	RESTORE_SOURCE_BRANCH {

		@Override
		public void operate(PullRequest request, String comment) {
			OneDev.getInstance(PullRequestManager.class).restoreSourceBranch(request, comment);
			Session.get().success("Restored source branch");
		}

		@Override
		public boolean canOperate(PullRequest request) {
			return request.getSourceProject() != null 
					&& request.getSource().getObjectName(false) == null 
					&& SecurityUtils.canModify(request) 
					&& SecurityUtils.canWriteCode(request.getSourceProject());
		}

	};
	
	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
	
}
