package com.pmease.gitop.core.manager.impl;

import static com.pmease.gitop.model.PullRequest.CriterionHelper.ofOpen;
import static com.pmease.gitop.model.PullRequest.CriterionHelper.ofSource;
import static com.pmease.gitop.model.PullRequest.CriterionHelper.ofTarget;
import static com.pmease.gitop.model.helper.IntegrationInfo.IntegrateApproach.MERGE_ALWAYS;
import static com.pmease.gitop.model.helper.IntegrationInfo.IntegrateApproach.MERGE_IF_NECESSARY;
import static com.pmease.gitop.model.helper.IntegrationInfo.IntegrateApproach.REBASE_SOURCE;
import static com.pmease.gitop.model.helper.IntegrationInfo.IntegrateApproach.REBASE_TARGET;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
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
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequest.Status;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.VoteInvitation;
import com.pmease.gitop.model.gatekeeper.checkresult.Approved;
import com.pmease.gitop.model.helper.BranchMatcher;
import com.pmease.gitop.model.helper.CloseInfo;
import com.pmease.gitop.model.helper.IntegrationInfo;
import com.pmease.gitop.model.integrationsetting.BranchStrategy;
import com.pmease.gitop.model.integrationsetting.IntegrationSetting;
import com.pmease.gitop.model.integrationsetting.IntegrationStrategy;

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
		Lock lock = LockUtils.lock(request.getLockName());
		try {
			Git git = request.getTarget().getRepository().git();
			String branchHead = request.getTarget().getHeadCommit();
			String requestHead = request.getLatestUpdate().getHeadCommit();
			
			String integrateRef = request.getIntegrateRef();
			
			if (git.isAncestor(requestHead, branchHead)) {
				CloseInfo closeInfo = new CloseInfo();
				closeInfo.setClosedBy(null);
				closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
				closeInfo.setComment("Target branch already contains commit of source branch.");
				request.setCloseInfo(closeInfo);
				request.setCheckResult(new Approved("Already integrated."));
				request.setIntegrationInfo(new IntegrationInfo(branchHead, requestHead, branchHead, null));
			} else {
				// Update head ref so that it can be pulled by build system
				git.updateRef(request.getHeadRef(), requestHead, null, null);

				IntegrationStrategy strategy = getIntegrationStrategy(request);
				
				IntegrationInfo.IntegrateApproach approach = null;
				if (strategy.isTryRebaseFirst()) {
					if (request.isToUpstream()) {
						Branch source = request.getSource();
						if (source != null) {
							BranchMatcher rebasibleBranches = source.getRepository()
									.getIntegrationSetting().getRebasibleBranches();
							if (rebasibleBranches != null && rebasibleBranches.matches(source))
								approach = REBASE_SOURCE;
						} else {
							approach = REBASE_SOURCE;
						}
					} else {
						BranchMatcher rebasibleBranches = request.getTarget().getRepository()
								.getIntegrationSetting().getRebasibleBranches();
						if (rebasibleBranches != null && rebasibleBranches.matches(request.getTarget()))
							approach = REBASE_TARGET;
					}
				} 
				if (approach == null) {
					if (strategy.isMergeAlwaysOtherwise())
						approach = MERGE_ALWAYS;
					else
						approach = MERGE_IF_NECESSARY;
				}
				
				if (request.getIntegrationInfo() == null 
						|| !branchHead.equals(request.getIntegrationInfo().getBranchHead())
						|| !requestHead.equals(request.getIntegrationInfo().getRequestHead())
						|| approach != request.getIntegrationInfo().getIntegrateApproach()
						|| request.getIntegrationInfo().getIntegrationHead() != null 
								&& !request.getIntegrationInfo().getIntegrationHead().equals(git.parseRevision(integrateRef, false))) {
					
					if (approach == MERGE_IF_NECESSARY && git.isAncestor(branchHead, requestHead)) {
						request.setIntegrationInfo(new IntegrationInfo(branchHead, requestHead, requestHead, approach));
						git.updateRef(integrateRef, requestHead, null, null);
					} else {
						File tempDir = FileUtils.createTempDir();
						try {
							Git tempGit = new Git(tempDir);
							tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, 
									request.getTarget().getName());
							
							String integrateHead;
							if (approach == REBASE_TARGET) {
								tempGit.updateRef("HEAD", requestHead, null, null);
								tempGit.reset(null, null);
								integrateHead = tempGit.cherryPick(".." + branchHead);
							} else {
								tempGit.updateRef("HEAD", branchHead, null, null);
								tempGit.reset(null, null);
								if (approach == REBASE_SOURCE) {
									integrateHead = tempGit.cherryPick(".." + requestHead);
								} else {
									FastForwardMode fastForwardMode;
									if (approach == MERGE_ALWAYS)
										fastForwardMode = FastForwardMode.NO_FF;
									else 
										fastForwardMode = FastForwardMode.FF;
									integrateHead = tempGit.merge(requestHead, fastForwardMode, null, null, 
											"Merge integration request: " + request.getTitle());
								}
							}
							
							request.setIntegrationInfo(new IntegrationInfo(branchHead, requestHead, integrateHead, approach));
							
							if (integrateHead != null)
								git.fetch(tempGit, "+HEAD:" + integrateRef);
							else
								git.deleteRef(integrateRef, null, null);
						} finally {
							FileUtils.deleteDir(tempDir);
						}
					}
				}
				request.setCheckResult(request.getTarget().getRepository().getGateKeeper().checkRequest(request));

				for (VoteInvitation invitation : request.getVoteInvitations()) {
					if (!request.getCheckResult().canVote(invitation.getVoter(), request))
						dao.remove(invitation);
					else if (invitation.getId() == null)
						dao.persist(invitation);
				}
			}
	
			dao.persist(request);
			
			if (request.isAutoMerge())
				integrate(request, null, "Integrated automatically by system.");
		} finally {
			lock.unlock();
		}
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
	public boolean integrate(final PullRequest request, final User user, final String comment) {
		Lock lock = LockUtils.lock(request.getLockName());
		try {
			if (request.getStatus() == Status.PENDING_INTEGRATE) {
				Preconditions.checkNotNull(request.getIntegrationInfo());
				
				Long userId = (user != null?user.getId():null);
				
				if (request.getIntegrationInfo().getIntegrateApproach() == REBASE_SOURCE 
						&& request.getSource() != null) {
					Git sourceGit = request.getSource().getRepository().git();
					if (!sourceGit.updateRef(request.getSource().getHeadRef(), 
							request.getIntegrationInfo().getIntegrationHead(), 
							request.getIntegrationInfo().getRequestHead(), "Rebase for integration request: " +request.getId())) {
						return false;
					} else {
						onBranchRefUpdate(request.getSource().getId(), userId, request.getId());
					}
				}
				Git git = request.getTarget().getRepository().git();
				if (git.updateRef(request.getTarget().getHeadRef(), 
						request.getIntegrationInfo().getIntegrationHead(), 
						request.getIntegrationInfo().getBranchHead(), 
						comment!=null?comment:"Integration request #" + request.getId())) {
					onBranchRefUpdate(request.getTarget().getId(), userId, request.getId());

					CloseInfo closeInfo = new CloseInfo();
					closeInfo.setClosedBy(user);
					closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
					closeInfo.setComment(comment);
					request.setCloseInfo(closeInfo);
					dao.persist(request);

					return true;
				}
				
			}
			return false;
		} finally {
			lock.unlock();
		}
	}
	
	private void onBranchRefUpdate(final Long branchId, @Nullable final Long userId, final Long requestId) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						Branch branch = dao.load(Branch.class, branchId);
						User user = (userId!=null?dao.load(User.class, userId):null);
						PullRequest request = dao.load(PullRequest.class, requestId);
						branchManager.onBranchRefUpdate(branch, user, request);
						return null;
					}
					
				});
			}
			
		});
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
		dao.persist(request);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			pullRequestUpdateManager.save(update);
		}

		refresh(request);
	}

	private IntegrationStrategy getIntegrationStrategy(PullRequest request) {
		IntegrationSetting integrationSetting = request.getTarget().getRepository().getIntegrationSetting();
		IntegrationStrategy strategy = null;
		if (request.isToUpstream()) {
			for (BranchStrategy branchStrategy: integrationSetting.getDownstreamStrategies()) {
				if (branchStrategy.getTargetBranches().matches(request.getTarget())) {
					strategy = new IntegrationStrategy();
					strategy.setMergeAlwaysOtherwise(branchStrategy.isMergeAlwaysOtherwise());
					strategy.setTryRebaseFirst(branchStrategy.isTryRebaseFirst());
					break;
				}
			}
			if (strategy == null) 
				strategy = integrationSetting.getDefaultDownstreamStrategy();
		} else {
			for (BranchStrategy branchStrategy: integrationSetting.getUpstreamStrategies()) {
				if (branchStrategy.getTargetBranches().matches(request.getTarget())) {
					strategy = new IntegrationStrategy();
					strategy.setMergeAlwaysOtherwise(branchStrategy.isMergeAlwaysOtherwise());
					strategy.setTryRebaseFirst(branchStrategy.isTryRebaseFirst());
					break;
				}
			}
			if (strategy == null) 
				strategy = integrationSetting.getDefaultUpstreamStrategy();
		}
		return strategy;
	}
	
}
