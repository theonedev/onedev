package com.pmease.gitop.core.manager.impl;

import static com.pmease.gitop.model.PullRequest.CriterionHelper.ofOpen;
import static com.pmease.gitop.model.PullRequest.CriterionHelper.ofSource;
import static com.pmease.gitop.model.PullRequest.CriterionHelper.ofTarget;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.LockUtils;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.CloseInfo;
import com.pmease.gitop.model.MergeInfo;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequest.Status;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.VoteInvitation;
import com.pmease.gitop.model.gatekeeper.checkresult.Approved;

@Singleton
public class DefaultPullRequestManager implements PullRequestManager {

	private final Dao dao;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final BranchManager branchManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Executor executor;
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager, 
			BranchManager branchManager, UnitOfWork unitOfWork, Executor executor) {
		this.dao = dao;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.branchManager = branchManager;
		this.unitOfWork = unitOfWork;
		this.executor = executor;
	}

	@Sessional
	@Override
	public PullRequest findOpen(Branch target, Branch source) {
		return dao.find(EntityCriteria.of(PullRequest.class)
				.add(ofOpen())
				.add(ofTarget(target))
				.add(ofSource(source))
				.addOrder(Order.desc("id")));
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		deleteRefs(request);
		
		dao.remove(request);
	}

	@Sessional
	@Override
	public void deleteRefs(PullRequest request) {
		for (PullRequestUpdate update : request.getUpdates())
			update.deleteRefs();
		
		request.deleteRefs();
	}
	
	/**
	 * Refresh internal state of this pull request. Pull request should be refreshed when:
	 * 
	 * <li> It is updated 
	 * <li> Head of target branch changes
	 * <li> Some one vote against it
	 * <li> CI system reports completion of build against relevant commits
	 */
	@Sessional
	public void refresh(final PullRequest request) {
		Preconditions.checkNotNull(request.getId());
		
		Lock lock = LockUtils.lock(request.getLockName());
		try {
			Git git = request.getTarget().getRepository().git();
			String branchHead = request.getTarget().getHeadCommit();
			String requestHead = request.getLatestUpdate().getHeadCommit();
			
			String mergeRef = request.getMergeRef();
			
			if (git.isAncestor(requestHead, branchHead)) {
				CloseInfo closeInfo = new CloseInfo();
				closeInfo.setClosedBy(null);
				closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
				closeInfo.setComment("Target branch already contains commit of source branch.");
				request.setCloseInfo(closeInfo);
				request.setCheckResult(new Approved("Already integrated."));
				request.setMergeInfo(new MergeInfo(branchHead, requestHead, requestHead, branchHead));
			} else {
				// Update head ref so that it can be pulled by build system
				git.updateRef(request.getHeadRef(), requestHead, null, null);
				
				if (git.isAncestor(branchHead, requestHead)) {
					request.setMergeInfo(new MergeInfo(branchHead, requestHead, branchHead, requestHead));
					git.updateRef(mergeRef, requestHead, null, null);
				} else {
					if (request.getMergeInfo() != null 
							&& (!request.getMergeInfo().getBranchHead().equals(branchHead) 
									|| !request.getMergeInfo().getRequestHead().equals(requestHead))) {
						 // Commits for merging have been changed since last merge, we have to
						 // re-merge 
						request.setMergeInfo(null);
					}
					if (request.getMergeInfo() != null && request.getMergeInfo().getMergeHead() != null 
							&& !request.getMergeInfo().getMergeHead().equals(git.parseRevision(mergeRef, false))) {
						 // Commits for merging have not been changed since last merge, but recorded 
						 // merge is incorrect in repository, so we have to re-merge 
						request.setMergeInfo(null);
					}
					if (request.getMergeInfo() == null) {
						String mergeBase = git.calcMergeBase(branchHead, requestHead);
						
						File tempDir = FileUtils.createTempDir();
						try {
							Git tempGit = new Git(tempDir);
							
							// Branch name here is not significant, we just use an existing branch
							// in cloned repository to hold mergeBase, so that we can merge with 
							// previousUpdate 
							String branchName = request.getTarget().getName();
							tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, branchName);
							tempGit.updateRef("HEAD", branchHead, null, null);
							tempGit.reset(null, null);
							
							if (tempGit.merge(requestHead, null, null, null)) {
								git.fetch(tempGit.repoDir().getAbsolutePath(), "+HEAD:" + mergeRef);
								request.setMergeInfo(new MergeInfo(branchHead, requestHead, 
										mergeBase, git.parseRevision(mergeRef, true)));
							} else {
								request.setMergeInfo(new MergeInfo(branchHead, requestHead, mergeBase, null));
							}
						} finally {
							FileUtils.deleteDir(tempDir);
						}
					}
				}
				
				request.setCheckResult(request.getTarget().getRepository().getGateKeeper().checkRequest(request));

				inviteToVote(request);
			}
	
			dao.persist(request);
			
			if (request.isAutoMerge())
				merge(request, null, "Integrated automatically by system.");
		} finally {
			lock.unlock();
		}
	}

	@Sessional
	public PullRequest preview(Branch target, Branch source, User submitter, File sandbox) {
		PullRequest request = new PullRequest();
		request.setTarget(target);
		request.setSource(source);
		request.setSubmitter(submitter);
		
		PullRequestUpdate update = new PullRequestUpdate();
		request.getUpdates().add(update);
		update.setRequest(request);
		update.setUser(submitter);
		
		request.getUpdates().add(update);
		update.setHeadCommit(source.getHeadCommit());
		request.setUpdateDate(new Date());
		
    	String targetHead = target.getHeadCommit();
		request.setBaseCommit(targetHead);
		String sourceHead = source.getHeadCommit();

		if (target.getRepository().equals(source.getRepository())) {
			if (target.getRepository().git().isAncestor(sourceHead, targetHead)) {
				CloseInfo closeInfo = new CloseInfo();
				closeInfo.setClosedBy(null);
				closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
				closeInfo.setComment("Target branch already contains commit of source branch.");
				request.setCloseInfo(closeInfo);
				request.setCheckResult(new Approved("Already integrated."));
				request.setMergeInfo(new MergeInfo(targetHead, sourceHead, sourceHead, targetHead));
			} else {
				if (target.getRepository().git().isAncestor(targetHead, sourceHead)) {
					request.setMergeInfo(new MergeInfo(targetHead, sourceHead, targetHead, sourceHead));
				} else {
					Git git = new Git(sandbox);
					git.clone(target.getRepository().git().repoDir().getAbsolutePath(), 
							false, true, true, request.getTarget().getName());
					git.updateRef("HEAD", targetHead, null, null);
					git.reset(null, null);

					request.setSandbox(git);
					
					String mergeBase = git.calcMergeBase(targetHead, sourceHead);
					if (git.merge(sourceHead, null, null, null))
						request.setMergeInfo(new MergeInfo(targetHead, sourceHead, mergeBase, git.parseRevision("HEAD", true)));
					else
						request.setMergeInfo(new MergeInfo(targetHead, sourceHead, mergeBase, null));
				}
				
				request.setCheckResult(target.getRepository().getGateKeeper().checkRequest(request));
			}
		} else {
			Git git = new Git(sandbox);
			git.clone(request.getTarget().getRepository().git().repoDir().getAbsolutePath(), 
					false, true, true, request.getTarget().getName());
			git.updateRef("HEAD", targetHead, null, null);
			git.reset(null, null);

			request.setSandbox(git);

			git.fetch(source.getRepository().git().repoDir().getAbsolutePath(), source.getHeadRef());
			
			if (git.isAncestor(sourceHead, targetHead)) {
				CloseInfo closeInfo = new CloseInfo();
				closeInfo.setClosedBy(null);
				closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
				closeInfo.setComment("Target branch already contains commit of source branch.");
				request.setCloseInfo(closeInfo);
				request.setCheckResult(new Approved("Already integrated."));
				request.setMergeInfo(new MergeInfo(targetHead, sourceHead, sourceHead, targetHead));
			} else {
				if (git.isAncestor(targetHead, sourceHead)) {
					request.setMergeInfo(new MergeInfo(targetHead, sourceHead, targetHead, sourceHead));
				} else {
					String mergeBase = git.calcMergeBase(targetHead, sourceHead);
					if (git.merge(sourceHead, null, null, null))
						request.setMergeInfo(new MergeInfo(targetHead, sourceHead, mergeBase, git.parseRevision("HEAD", true)));
					else
						request.setMergeInfo(new MergeInfo(targetHead, sourceHead, mergeBase, null));
				}
				
				request.setCheckResult(target.getRepository().getGateKeeper().checkRequest(request));
			}
			
		}
		
		return request;
	}

	@Transactional
 	public void discard(PullRequest request, final User user, final String comment) {
		Lock lock = LockUtils.lock(request.getLockName());
		try {
			CloseInfo closeInfo = new CloseInfo();
			closeInfo.setClosedBy(user);
			closeInfo.setCloseStatus(CloseInfo.Status.DISCARDED);
			closeInfo.setComment(comment);
			request.setCloseInfo(closeInfo);
			dao.persist(request);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Merge specified request if possible.
	 * 
	 * @param request
	 * 			request to be merged
	 * @return
	 * 			<tt>true</tt> if successful, <tt>false</tt> otherwise. Reason of unsuccessful
	 * 			merge can be:
	 * 			<li> request is not in PENDING_MERGE status.
	 * 			<li> branch ref has just been updated in some other threads and this thread 
	 * 				is unable to lock the reference.
	 */
	@Transactional
	public boolean merge(final PullRequest request, final User user, final String comment) {
		Lock lock = LockUtils.lock(request.getLockName());
		try {
			if (request.getStatus() == Status.PENDING_INTEGRATE) {
				Git git = request.getTarget().getRepository().git();
				if (git.updateRef(request.getTarget().getHeadRef(), 
						request.getMergeInfo().getMergeHead(), 
						request.getMergeInfo().getBranchHead(), 
						comment!=null?comment:"merge pull request")) {

					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setClosedBy(user);
					closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
					closeInfo.setComment(comment);
					request.setCloseInfo(closeInfo);
					dao.persist(request);

					final Long branchId = request.getTarget().getId();
					final Long userId;
					if (user != null)
						userId = user.getId();
					else
						userId = null;
					executor.execute(new Runnable() {

						@Override
						public void run() {
							unitOfWork.call(new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									Branch branch = dao.load(Branch.class, branchId);
									User user;
									if (userId != null)
										user = dao.load(User.class, userId);
									else
										user = null;
									branchManager.onBranchRefUpdate(branch, user);
									return null;
								}
								
							});
						}
						
					});
					return true;
				}
				
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	@Sessional
	@Override
	public List<PullRequest> findByCommit(String commit) {
		return dao.query(EntityCriteria.of(PullRequest.class)
				.add(Restrictions.or(
						Restrictions.eq("mergeInfo.requestHead", commit), 
						Restrictions.eq("mergeInfo.merged", commit))), 0, 0);
	}

	@Transactional
	@Override
	public void send(PullRequest request) {
		Preconditions.checkArgument(request.getId() == null);
		
		dao.persist(request);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			pullRequestUpdateManager.save(update);
		}

		Git targetGit = request.getTarget().getRepository().git();
		
		// Update head ref so that it can be pulled by build system
		targetGit.updateRef(request.getHeadRef(), request.getLatestUpdate().getHeadCommit(), null, null);

		String mergeHead = request.getMergeInfo().getMergeHead();
		if (mergeHead != null) {
			if (mergeHead.equals(request.getLatestUpdate().getHeadCommit()))
				targetGit.updateRef(request.getMergeRef(), mergeHead, null, null);
 			else
				targetGit.fetch(request.git().repoDir().getAbsolutePath(), "+HEAD:" + request.getMergeRef());
		}

		inviteToVote(request);
	}
 	
	private void inviteToVote(PullRequest request) {
		for (VoteInvitation invitation : request.getVoteInvitations()) {
			if (!request.getCheckResult().canVote(invitation.getVoter(), request))
				dao.remove(invitation);
			else if (invitation.getId() == null)
				dao.persist(invitation);
		}
	}

}
