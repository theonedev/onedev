package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail;

import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.Status;
import com.pmease.gitplex.core.entity.PullRequestReview;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;
import com.pmease.gitplex.core.entity.support.IntegrationPreview;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestReviewManager;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
public enum PullRequestOperation {
	INTEGRATE {

		@Override
		public boolean canOperate(PullRequest request) {
			if (!SecurityUtils.canWrite(request.getTargetDepot()) || request.getStatus() != PENDING_INTEGRATE) {
				return false;
			} else {
				IntegrationPreview integrationPreview = request.getIntegrationPreview();
				return integrationPreview != null && integrationPreview.getIntegrated() != null;
			}
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestManager.class).integrate(request, comment);
		}

		@Override
		public Component newHinter(String id, PullRequest request) {
			Long requestId = request.getId();
			return new IntegrationHintPanel(id, new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					return GitPlex.getInstance(PullRequestManager.class).load(requestId);
				}
				
			});
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
			GitPlex.getInstance(PullRequestReviewManager.class).review(request, PullRequestReview.Result.APPROVE, comment);
		}

		@Override
		public Component newHinter(String id, PullRequest request) {
			Long requestId = request.getId();
			return new ApprovalHintPanel(id, new LoadableDetachableModel<PullRequest>() {

				@Override
				protected PullRequest load() {
					return GitPlex.getInstance(PullRequestManager.class).load(requestId);
				}
				
			});
		}
		
	},
	DISAPPROVE {

		@Override
		public boolean canOperate(PullRequest request) {
			return canReview(request);
		}

		@Override
		public void operate(PullRequest request, String comment) {
			GitPlex.getInstance(PullRequestReviewManager.class).review(request, PullRequestReview.Result.DISAPPROVE, comment);
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
			return !GitUtils.isMergedInto(request.getTargetDepot().getRepository(), 
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
					&& pullRequestManager.findAllOpenTo(request.getSource(), null).isEmpty();
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
					&& SecurityUtils.canPushRef(request.getSourceDepot(), request.getSourceRef(), ObjectId.zeroId(), ObjectId.fromString(request.getHeadCommitHash()));
		}

	};
	
	private static boolean canReview(PullRequest request) {
		Account user = GitPlex.getInstance(AccountManager.class).getCurrent();
		
		// call request.getStatus() in order to trigger generation of review
		// integrations which will be used in else condition 
		if (user == null  
				|| request.getStatus() == PullRequest.Status.INTEGRATED 
				|| request.getStatus() == PullRequest.Status.DISCARDED
				|| request.isReviewEffective(user)) { 
			return false;
		} else {
			for (PullRequestReviewInvitation invitation: request.getReviewInvitations()) {
				if (invitation.getStatus() != PullRequestReviewInvitation.Status.EXCLUDED && invitation.getUser().equals(user))
					return true;
			}
			return false;
		}
	}

	public abstract void operate(PullRequest request, @Nullable String comment);
	
	public abstract boolean canOperate(PullRequest request);	
	
	public Component newHinter(String id, PullRequest request) {
		return new WebMarkupContainer(id).setVisible(false);
	}
	
}
