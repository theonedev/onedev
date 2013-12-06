package com.pmease.gitop.core.manager.impl;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.EntityEvent;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.LockUtils;
import com.pmease.gitop.core.event.BranchRefUpdateEvent;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.BuildResult;
import com.pmease.gitop.model.MergePrediction;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequest.Status;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.VoteInvitation;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.model.gatekeeper.checkresult.Pending;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@Singleton
public class DefaultPullRequestManager extends AbstractGenericDao<PullRequest> implements
		PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private final VoteInvitationManager voteInvitationManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final UnitOfWork unitOfWork;
	
	private final EventBus eventBus;
	
	private final Executor executor;
	
	@Inject
	public DefaultPullRequestManager(GeneralDao generalDao, VoteInvitationManager voteInvitationManager,
			PullRequestUpdateManager pullRequestUpdateManager, UnitOfWork unitOfWork, EventBus eventBus, 
			Executor executor) {
		super(generalDao);
		this.voteInvitationManager = voteInvitationManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.unitOfWork = unitOfWork;
		this.eventBus = eventBus;
		this.executor = executor;
		eventBus.register(this);
	}

	@Sessional
	@Override
	public PullRequest findOpen(Branch target, Branch source) {
		Criterion statusCriterion =
				Restrictions.and(
						Restrictions.not(Restrictions.eq("status", PullRequest.Status.MERGED)),
						Restrictions.not(Restrictions.eq("status", PullRequest.Status.DECLINED))
					);

		Criterion[] criterions = new Criterion[]{
				Restrictions.eq("target", target), 
				Restrictions.eq("source", source),
				statusCriterion};
		
		return find(criterions, new Order[]{Order.desc("id")});
	}

	@Transactional
	@Override
	public void delete(final PullRequest request) {
		for (PullRequestUpdate update : request.getUpdates())
			update.deleteRefs();
		
		request.deleteRefs();

		super.delete(request);
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
		LockUtils.call(request.getLockName(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Git git = request.getTarget().getProject().code();
				String branchHead = request.getTarget().getHeadCommit();
				String requestHead = request.getLatestUpdate().getHeadCommit();
				
				String mergeRef = request.getMergeRef();
				
				if (git.isAncestor(requestHead, branchHead)) {
					request.setStatus(Status.MERGED);
				} else {
					// Update head ref so that it can be pulled by build system
					git.updateRef(request.getHeadRef(), requestHead, null, null);
					
					if (git.isAncestor(branchHead, requestHead)) {
						request.setMergePrediction(new MergePrediction(branchHead, requestHead, requestHead));
						git.updateRef(mergeRef, requestHead, null, null);
					} else {
						if (request.getMergePrediction() != null 
								&& (!request.getMergePrediction().getBranchHead().equals(branchHead) 
										|| !request.getMergePrediction().getRequestHead().equals(requestHead))) {
							 // Commits for merging have been changed since last merge, we have to
							 // re-merge 
							request.setMergePrediction(null);
						}
						if (request.getMergePrediction() != null && request.getMergePrediction().getMerged() != null 
								&& !request.getMergePrediction().getMerged().equals(git.resolveRef(mergeRef, false))) {
							 // Commits for merging have not been changed since last merge, but recorded 
							 // merge is incorrect in repository, so we have to re-merge 
							request.setMergePrediction(null);
						}
						if (request.getMergePrediction() == null) {
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
									request.setMergePrediction(new MergePrediction(branchHead, requestHead, git.resolveRef(mergeRef, true)));
								} else {
									request.setMergePrediction(new MergePrediction(branchHead, requestHead, null));
								}
							} finally {
								FileUtils.deleteDir(tempDir);
							}
						}
					}
					
					GateKeeper gateKeeper = request.getTarget().getProject().getGateKeeper();
					request.setCheckResult(gateKeeper.check(request));
			
					for (VoteInvitation invitation : request.getVoteInvitations()) {
						if (!request.getCheckResult().canVote(invitation.getVoter(), request))
							voteInvitationManager.delete(invitation);
					}
			
					Preconditions.checkNotNull(request.getMergePrediction());
					
					if (request.getCheckResult() instanceof Pending || request.getCheckResult() instanceof Blocked) {
						request.setStatus(Status.PENDING_APPROVAL);
					} else if (request.getCheckResult() instanceof Rejected) {
						request.setStatus(Status.PENDING_UPDATE);
					} else if (request.getMergePrediction().getMerged() == null) {
						request.setStatus(Status.PENDING_UPDATE);
					} else {
						request.setStatus(Status.PENDING_MERGE);
					}
				}
		
				save(request);
				
				if (request.isAutoMerge())
					merge(request);
				
				return null;
			}
			
		});
	}

	@Sessional
	public void decline(final PullRequest request) {
		LockUtils.call(request.getLockName(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				request.setStatus(Status.DECLINED);
				save(request);
				return null;
			}
			
		});
	}
	
	@Sessional
	public void reopen(final PullRequest request) {
		LockUtils.call(request.getLockName(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				request.setStatus(Status.PENDING_CHECK);
				request.setMergePrediction(null);
				request.setCheckResult(null);
				save(request);
				
				return null;
			}
		});
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
	@Sessional
	public boolean merge(final PullRequest request) {
		return LockUtils.call(request.getLockName(), new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				if (request.getStatus() == Status.PENDING_MERGE) {
					Git git = request.getTarget().getProject().code();
					if (git.updateRef(request.getTarget().getHeadRef(), request.getMergePrediction().getMerged(), 
							request.getMergePrediction().getBranchHead(), "merge pull request")) {
						request.setStatus(Status.MERGED);
						save(request);
						eventBus.post(new BranchRefUpdateEvent(request.getTarget()));
						return true;
					}
				}
				return false;
			}
			
		});
	}
	
	@Sessional
	@Subscribe
	public void refreshUpon(EntityEvent entityEvent) {
		if (entityEvent.getEntity() instanceof Vote) {
			Vote vote = (Vote) entityEvent.getEntity();
			if (vote.getUpdate().getRequest().isOpen())
				refresh(vote.getUpdate().getRequest());
		} else if (entityEvent.getEntity() instanceof PullRequestUpdate) {
			PullRequestUpdate update = (PullRequestUpdate) entityEvent.getEntity();
			if (update.getRequest().isOpen())
				refresh(update.getRequest());
		} else if (entityEvent.getEntity() instanceof BuildResult) {
			BuildResult result = (BuildResult) entityEvent.getEntity();
			for (PullRequest request: findByCommit(result.getCommit())) {
				if (request.isOpen()) 
					refresh(request);
			}
		}
	}
	
	@Sessional
	@Subscribe
	public void refreshUpon(BranchRefUpdateEvent event) {
		for (final PullRequest request: event.getBranch().getIngoingRequests()) {
			if (request.isOpen()) {
				executor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							unitOfWork.call(new Callable<Void>() {
	
								@Override
								public Void call() throws Exception {
										// Reload request to avoid Hibernate LazyInitializationException
										refresh(load(request.getId()));
										return null;
								}
								
							});
						} catch (Exception e) {
							logger.error("Error refreshing pull request.", e);
							throw ExceptionUtils.unchecked(e);
						}
					}
					
				});
			}
		}
	}
	
	@Sessional
	@Override
	public List<PullRequest> findByCommit(String commit) {
		return query(Restrictions.or(
				Restrictions.eq("mergeResult.requestHead", commit), 
				Restrictions.eq("mergeResult.merged", commit)));
	}

	@Transactional
	@Override
	public PullRequest create(String title, Branch target, Branch source, boolean autoMerge) {
		if (!target.equals(source)) {
			PullRequest request = new PullRequest();
			request.setAutoMerge(autoMerge);
			request.setSource(source);
			request.setTarget(target);
			request.setTitle(title);
	
			save(request);
			
			pullRequestUpdateManager.update(request);
			
			if (request.getUpdates().isEmpty()) {
				delete(request);
				return null;
			} else {
				return request;
			}
		} else {
			return null;
		}
	}

}
