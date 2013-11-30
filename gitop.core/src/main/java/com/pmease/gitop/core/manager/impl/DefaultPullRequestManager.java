package com.pmease.gitop.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequest.MergeResult;
import com.pmease.gitop.model.PullRequest.Status;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.VoteInvitation;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Pending;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@Singleton
public class DefaultPullRequestManager extends AbstractGenericDao<PullRequest> implements
		PullRequestManager {

	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	@Inject
	public DefaultPullRequestManager(GeneralDao generalDao, PullRequestUpdateManager pullRequestUpdateManager) {
		super(generalDao);
		this.pullRequestUpdateManager = pullRequestUpdateManager;
	}

	@Sessional
	@Override
	public PullRequest findOpen(Branch target, Branch source, User submitter) {
		Criterion statusCriterion =
				Restrictions.and(
						Restrictions.not(Restrictions.eq("status", PullRequest.Status.MERGED)),
						Restrictions.not(Restrictions.eq("status", PullRequest.Status.DECLINED))
					);

		return find(Restrictions.eq("target", target), Restrictions.eqOrIsNull("source", source),
				Restrictions.eq("submitter", submitter), statusCriterion);
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		for (PullRequestUpdate update : request.getUpdates())
			pullRequestUpdateManager.delete(update);
		super.delete(request);
	}

	/**
	 * Refresh internal state of this pull request. Pull request should be refreshed when:
	 * <li> It is updated 
	 * <li> Head of target branch changes
	 * <li> Some one vote against it
	 * <li> CI system reports completion of build against relevant commits 
	 */
	@Transactional
	public void refresh(PullRequest request) {
		Git git = request.getTarget().getProject().code();
		String branchHead = request.getTarget().getHeadCommit();
		String requestHead = request.getLatestUpdate().getHeadCommit();
			
		PullRequest.Status status;
		PullRequest.MergeResult mergeResult = request.getMergeResult();
		CheckResult checkResult = request.getCheckResult();
		
		String mergeRef = request.getMergeRef();
		
		if (git.isAncestor(requestHead, branchHead)) {
			status = Status.MERGED;
		} else {
			if (git.isAncestor(branchHead, requestHead)) {
				mergeResult = new MergeResult(branchHead, requestHead, requestHead);
				git.updateRef(mergeRef, requestHead, null, null);
			} else {
				if (mergeResult != null 
						&& (!mergeResult.getBranchHead().equals(branchHead) 
								|| !mergeResult.getRequestHead().equals(requestHead))) {
					 // Commits for merging have been changed since last merge, we have to
					 // re-merge 
					mergeResult = null;
				}
				if (mergeResult != null && mergeResult.getMerged() != null 
						&& !mergeResult.getMerged().equals(git.resolveRef(mergeRef, false))) {
					 // Commits for merging have not been changed since last merge, but recorded 
					 // merge is incorrect in repository, so we have to re-merge 
					mergeResult = null;
				}
				if (mergeResult == null) {
					File tempDir = FileUtils.createTempDir();
					try {
						Git tempGit = new Git(tempDir);
						
						 // Branch name here is not significant, we just use an existing branch
						 // in cloned repository to hold mergeBase, so that we can merge with 
						 // previousUpdate 
						String branchName = request.getTarget().getName();
						tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, branchName);
						tempGit.updateRef("HEAD", requestHead, null, null);
						tempGit.reset(null, null);
						if (tempGit.merge(branchHead, null, null, null)) {
							git.fetch(tempGit.repoDir().getAbsolutePath(), "+HEAD:" + mergeRef);
							mergeResult = new MergeResult(branchHead, requestHead, git.resolveRef(mergeRef, true));
						} else {
							mergeResult = new MergeResult(branchHead, requestHead, null);
						}
					} finally {
						FileUtils.deleteDir(tempDir);
					}
				}
			}
			
			GateKeeper gateKeeper = request.getTarget().getProject().getGateKeeper();
			checkResult = gateKeeper.check(request);
	
			VoteInvitationManager voteInvitationManager =
					Gitop.getInstance(VoteInvitationManager.class);
			for (VoteInvitation invitation : request.getVoteInvitations()) {
				if (!checkResult.canVote(invitation.getVoter(), request))
					voteInvitationManager.delete(invitation);
			}
	
			Preconditions.checkNotNull(mergeResult);
			
			if (checkResult instanceof Pending || checkResult instanceof Blocked) {
				status = Status.PENDING_APPROVAL;
			} else if (checkResult instanceof Rejected) {
				status = Status.PENDING_UPDATE;
			} else if (mergeResult.getMerged() == null) {
				status = Status.PENDING_UPDATE;
			} else {
				status = Status.PENDING_MERGE;
			}
		}

		request.setMergeResult(mergeResult);
		request.setCheckResult(checkResult);
		request.setStatus(status);

		save(request);
	}

	@Transactional
	public void decline(PullRequest request) {
		request.setStatus(Status.DECLINED);
		save(request);
	}
	
	@Transactional
	public boolean merge(PullRequest request) {
		refresh(request);

		if (request.getStatus() == Status.PENDING_MERGE) {
			Git git = request.getTarget().getProject().code();
			git.updateRef(request.getTarget().getHeadRef(), request.getMergeResult().getMerged(), 
					request.getTarget().getHeadCommit(), "merge pull request");
			request.setStatus(Status.MERGED);
			save(request);
			return true;
		} else {
			return false;
		}
	}
}
