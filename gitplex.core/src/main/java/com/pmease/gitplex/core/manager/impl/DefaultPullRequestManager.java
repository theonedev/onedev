package com.pmease.gitplex.core.manager.impl;

import static com.pmease.gitplex.core.entity.PullRequest.CriterionHelper.ofOpen;
import static com.pmease.gitplex.core.entity.PullRequest.CriterionHelper.ofSource;
import static com.pmease.gitplex.core.entity.PullRequest.CriterionHelper.ofTarget;
import static com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy.MERGE_WITH_SQUASH;
import static com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy.REBASE_SOURCE_ONTO_TARGET;
import static com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy.REBASE_TARGET_ONTO_SOURCE;
import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_APPROVAL;
import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitplex.core.entity.PullRequest.Status.PENDING_UPDATE;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.commons.util.match.PatternMatcher;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.entity.PullRequestActivity;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.component.CloseInfo;
import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;
import com.pmease.gitplex.core.entity.component.IntegrationPreview;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.listener.RefListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.PullRequestActivityManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.core.util.ChildAwareMatcher;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityDao<PullRequest> implements PullRequestManager, 
		DepotListener, RefListener, LifecycleListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private static final PatternMatcher BRANCH_MATCHER = new ChildAwareMatcher();
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final PullRequestCommentManager commentManager;
	
	private final AccountManager userManager;
	
	private final StorageManager storageManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	private final ReviewInvitationManager reviewInvitationManager;
	
	private final Set<Long> integrationPreviewCalculatingRequestIds = new ConcurrentHashSet<>();

	private final WorkManager workManager;
	
	private final PullRequestActivityManager pullRequestActivityManager;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();

	@Inject
	public DefaultPullRequestManager(Dao dao, 
			PullRequestUpdateManager pullRequestUpdateManager, StorageManager storageManager, 
			ReviewInvitationManager reviewInvitationManager, AccountManager userManager, 
			NotificationManager notificationManager, PullRequestCommentManager commentManager, 
			MarkdownManager markdownManager, WorkManager workManager, 
			UnitOfWork unitOfWork, Set<PullRequestListener> pullRequestListeners, 
			PullRequestActivityManager pullRequestActivityManager) {
		super(dao);
		
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.storageManager = storageManager;
		this.reviewInvitationManager = reviewInvitationManager;
		this.commentManager = commentManager;
		this.userManager = userManager;
		this.workManager = workManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestListeners = pullRequestListeners;
		this.pullRequestActivityManager = pullRequestActivityManager;
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		deleteRefs(request);
		
		remove(request);
	}

	@Sessional
	@Override
	public void deleteRefs(PullRequest request) {
		for (PullRequestUpdate update : request.getUpdates())
			update.deleteRefs();
		
		request.deleteRefs();
	}

	@Transactional
	@Override
	public void restoreSourceBranch(PullRequest request) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null);

		if (request.getSource().getObjectName(false) == null) {
			String latestCommitHash = request.getLatestUpdate().getHeadCommitHash();
			request.getSourceDepot().git().createBranch(
					request.getSourceBranch(), latestCommitHash);
			request.getSourceDepot().cacheObjectId(request.getSourceBranch(), ObjectId.fromString(latestCommitHash));
			PullRequestActivity activity = new PullRequestActivity();
			activity.setRequest(request);
			activity.setAction(PullRequestActivity.Action.RESTORE_SOURCE_BRANCH);
			activity.setDate(new Date());
			activity.setUser(GitPlex.getInstance(AccountManager.class).getCurrent());
			pullRequestActivityManager.persist(activity);
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			request.getSource().delete();
			
			PullRequestActivity activity = new PullRequestActivity();
			activity.setRequest(request);
			activity.setAction(PullRequestActivity.Action.DELETE_SOURCE_BRANCH);
			activity.setDate(new Date());
			activity.setUser(GitPlex.getInstance(AccountManager.class).getCurrent());
			pullRequestActivityManager.persist(activity);
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String comment) {
		Preconditions.checkState(!request.isOpen());
		
		Account user = userManager.getCurrent();
		request.setCloseInfo(null);
		request.setSubmitter(user);
		request.setSubmitDate(new Date());
		
		persist(request);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new DateTime(request.getSubmitDate()).minusSeconds(1).toDate());
		activity.setAction(PullRequestActivity.Action.REOPEN);
		activity.setUser(user);
		
		pullRequestActivityManager.persist(activity);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setRequest(request);
			requestComment.setUser(user);
			commentManager.save(requestComment);
		}

		onSourceBranchUpdate(request, false);
		
		if (request.isOpen()) {
			for (PullRequestListener listener: pullRequestListeners)
				listener.onReopenRequest(request, user, comment);
		}
	}

	@Transactional
	@Override
 	public void discard(PullRequest request, final String comment) {
		Account user = userManager.getCurrent();
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setAction(PullRequestActivity.Action.DISCARD);
		activity.setUser(user);
		
		pullRequestActivityManager.persist(activity);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setRequest(request);
			requestComment.setUser(user);
			
			commentManager.save(requestComment);
		}

		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(activity.getDate());
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.DISCARDED);
		request.setCloseInfo(closeInfo);
		request.setLastEventDate(activity.getDate());
		persist(request);
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onDiscardRequest(request, user, comment);
	}
	
	@Transactional
	@Override
	public void integrate(PullRequest request, String comment) {
		if (request.getStatus() != PENDING_INTEGRATE)
			throw new IllegalStateException("Gate keeper disallows integration right now.");
	
		IntegrationPreview preview = request.getIntegrationPreview();
		if (preview == null)
			throw new IllegalStateException("Integration preview has not been calculated yet.");

		String integrated = preview.getIntegrated();
		if (integrated == null)
			throw new IllegalStateException("There are integration conflicts.");
		
		Account user = userManager.getCurrent();

		Depot targetDepot = request.getTargetDepot();
		Git git = targetDepot.git();
		IntegrationStrategy strategy = request.getIntegrationStrategy();
		if ((strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY || strategy == MERGE_WITH_SQUASH) 
				&& !preview.getIntegrated().equals(preview.getRequestHead()) && comment != null) {
			File tempDir = FileUtils.createTempDir();
			try {
				Git tempGit = new Git(tempDir);
				tempGit.clone(git.depotDir().getAbsolutePath(), false, true, true, request.getTargetBranch());
				tempGit.updateRef("HEAD", preview.getIntegrated(), null, null);
				tempGit.reset(null, null);
				
				tempGit.commit(comment, false, true);
				integrated = tempGit.parseRevision("HEAD", true);
				git.fetch(tempGit, "+HEAD:" + request.getIntegrateRef());									
				comment = null;
			} finally {
				FileUtils.deleteDir(tempDir);
			}
		}
		ObjectId integratedCommit = ObjectId.fromString(integrated);
		if (strategy == REBASE_SOURCE_ONTO_TARGET || strategy == MERGE_WITH_SQUASH) {
			Depot sourceDepot = request.getSourceDepot();
			Git sourceGit = sourceDepot.git();
			String sourceRef = request.getSourceRef();
			sourceGit.updateRef(sourceRef, integrated, preview.getRequestHead(), 
					"Pull request #" + request.getNumber());
			sourceDepot.cacheObjectId(request.getSourceRef(), integratedCommit);
			onRefUpdate(sourceDepot, sourceRef, ObjectId.fromString(preview.getRequestHead()), integratedCommit);
		}
		
		String targetRef = request.getTargetRef();
		git.updateRef(targetRef, integrated, preview.getTargetHead(), "Pull request #" + request.getNumber());
		targetDepot.cacheObjectId(request.getTargetRef(), ObjectId.fromString(integrated));
		onRefUpdate(targetDepot, targetRef, ObjectId.fromString(preview.getTargetHead()), integratedCommit);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setAction(PullRequestActivity.Action.INTEGRATE);
		activity.setUser(user);
		
		pullRequestActivityManager.persist(activity);

		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setRequest(request);
			requestComment.setUser(user);
			commentManager.save(requestComment);
		}

		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(activity.getDate());
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
		request.setCloseInfo(closeInfo);
		
		request.setLastEventDate(activity.getDate());

		persist(request);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onIntegrateRequest(request, user, comment);
	}
	
	private long getNextNumber(Depot depot) {
		AtomicLong nextNumber;
		synchronized (nextNumbers) {
			nextNumber = nextNumbers.get(depot.getUUID());
		}
		if (nextNumber == null) {
			long maxNumber;
			Query query = getSession().createQuery("select max(number) from PullRequest where targetDepot=:depot");
			query.setParameter("depot", depot);
			Object result = query.uniqueResult();
			if (result != null) {
				maxNumber = (Long)result;
			} else {
				maxNumber = 0;
			}
			
			/*
			 * do not put the whole method in synchronized block to avoid possible deadlocks
			 * if there are limited connections. 
			 */
			synchronized (nextNumbers) {
				nextNumber = nextNumbers.get(depot.getUUID());
				if (nextNumber == null) {
					nextNumber = new AtomicLong(maxNumber+1);
					nextNumbers.put(depot.getUUID(), nextNumber);
				}
			}
		} 
		return nextNumber.getAndIncrement();
	}
	
	@Transactional
	@Override
	public void open(PullRequest request, Object listenerData) {
		request.setNumber(getNextNumber(request.getTargetDepot()));
		persist(request);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setDate(request.getSubmitDate());
		activity.setAction(PullRequestActivity.Action.OPEN);
		activity.setRequest(request);
		activity.setUser(request.getSubmitter());
		pullRequestActivityManager.persist(activity);
		
		FileUtils.cleanDir(storageManager.getCacheDir(request));
		
		request.git().updateRef(request.getBaseRef(), request.getBaseCommitHash(), null, null);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			update.setDate(new DateTime(activity.getDate()).plusSeconds(1).toDate());
			pullRequestUpdateManager.save(update, false);
		}
		
		for (ReviewInvitation invitation: request.getReviewInvitations())
			reviewInvitationManager.save(invitation);

		for (PullRequestListener listener: pullRequestListeners)
			listener.onOpenRequest(request);
		
		afterCommit(new Runnable() {

			@Override
			public void run() {
				IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
				workManager.remove(task);
				workManager.execute(task);
			}
			
		});
	}

	@Override
	public List<IntegrationStrategy> getApplicableIntegrationStrategies(PullRequest request) {
		List<IntegrationStrategy> strategies = null;
		for (IntegrationPolicy policy: request.getTargetDepot().getIntegrationPolicies()) {
			if (IncludeExcludeUtils.getIncludeExclude(policy.getTargetBranchMatch()).matches(BRANCH_MATCHER, request.getTargetBranch()) 
					&& FullBranchMatchUtils.matches(policy.getSourceBranchMatch(), request.getTargetDepot(), request.getSource())) {
				strategies = policy.getIntegrationStrategies();
				break;
			}
		}
		if (strategies == null) 
			strategies = Lists.newArrayList(IntegrationStrategy.MERGE_ALWAYS);
		return strategies;
	}

	@Transactional
	@Override
	public void onAssigneeChange(PullRequest request) {
		persist(request);
		for (PullRequestListener listener: pullRequestListeners)
			listener.onAssignRequest(request);
	}
	
	@Transactional
	@Override
	public void onTargetBranchUpdate(PullRequest request) {
		String targetHead = request.getTarget().getObjectName();
		if (request.getLastIntegrationPreview() == null || !request.getLastIntegrationPreview().getTargetHead().equals(targetHead)) {
			closeIfMerged(request);
			if (request.isOpen()) {
				IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
				workManager.remove(task);
				workManager.execute(task);
			}
		}
	}

	@Transactional
	private void closeIfMerged(PullRequest request) {
		if (request.getTargetDepot().isAncestor(request.getLatestUpdate().getHeadCommitHash(), request.getTarget().getObjectName())) {
			PullRequestActivity activity = new PullRequestActivity();
			activity.setRequest(request);
			activity.setUser(GitPlex.getInstance(AccountManager.class).getRoot());
			activity.setAction(PullRequestActivity.Action.INTEGRATE);
			activity.setDate(new Date());
			pullRequestActivityManager.persist(activity);
			
			request.setLastIntegrationPreview(null);
			CloseInfo closeInfo = new CloseInfo();
			closeInfo.setCloseDate(activity.getDate());
			closeInfo.setClosedBy(activity.getUser());
			closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
			request.setCloseInfo(closeInfo);
			request.setLastEventDate(activity.getDate());
			
			persist(request);
			
			for (PullRequestListener listener: pullRequestListeners)
				listener.onIntegrateRequest(request, null, null);
		} 
	}

	@Transactional
	@Override
	public void onSourceBranchUpdate(PullRequest request, boolean notify) {
		if (!request.getLatestUpdate().getHeadCommitHash().equals(request.getSource().getObjectName())) {
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			update.setHeadCommitHash(request.getSource().getObjectName());
			
			request.addUpdate(update);
			pullRequestUpdateManager.save(update, notify);
			closeIfMerged(request);

			if (request.isOpen()) {
				Long requestId = request.getId();
				afterCommit(new Runnable() {

					@Override
					public void run() {
						unitOfWork.asyncCall(new Runnable() {

							@Override
							public void run() {
								check(load(requestId));
							}
							
						});
					}
					
				});
			}
		}
	}

	/**
	 * This method might take some time and is not key to pull request logic (even if we 
	 * did not call it, we can always call it later), so normally should be called in an 
	 * executor
	 */
	@Transactional
	@Override
	public void check(PullRequest request) {
		Date now = new Date();
		if (request.isOpen()) {
			closeIfMerged(request);
			if (request.isOpen()) {
				if (request.getStatus() == PENDING_UPDATE) {
					for (PullRequestListener listener: pullRequestListeners)
						listener.pendingUpdate(request);
				} else if (request.getStatus() == PENDING_INTEGRATE) {
					for (PullRequestListener listener: pullRequestListeners)
						listener.pendingIntegration(request);
					
					IntegrationPreview integrationPreview = request.getIntegrationPreview();
					if (integrationPreview != null 
							&& integrationPreview.getIntegrated() != null 
							&& request.getAssignee() == null) {
						integrate(request, "Integrated automatically by system");
					}
				} else if (request.getStatus() == PENDING_APPROVAL) {
					for (PullRequestListener listener: pullRequestListeners)
						listener.pendingApproval(request);
					
					for (ReviewInvitation invitation: request.getReviewInvitations()) { 
						if (!invitation.getDate().before(now))
							reviewInvitationManager.save(invitation);
					}
				}
			}
		}
	}

	@Override
	public boolean canIntegrate(PullRequest request) {
		if (request.getStatus() != PENDING_INTEGRATE) {
			return false;
		} else {
			IntegrationPreview integrationPreview = request.getIntegrationPreview();
			return integrationPreview != null && integrationPreview.getIntegrated() != null;
		}
	}

	@Override
	public IntegrationPreview previewIntegration(PullRequest request) {
		IntegrationPreview preview = request.getLastIntegrationPreview();
		if (request.isOpen() && (preview == null || preview.isObsolete(request))) {
			IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
			workManager.remove(task);
			workManager.execute(task);
			return null;
		} else {
			return preview;
		}
	}
	
	private class IntegrationPreviewTask extends PrioritizedRunnable {

		private final Long requestId;
		
		public IntegrationPreviewTask(Long requestId) {
			super(RequestCycle.get() != null?UI_PREVIEW_PRIORITY:BACKEND_PREVIEW_PRIORITY);
			this.requestId = requestId;
		}
		
		@Override
		public void run() {
			unitOfWork.begin();
			try {
				if (!integrationPreviewCalculatingRequestIds.contains(requestId)) {
					integrationPreviewCalculatingRequestIds.add(requestId);
					try {
						PullRequest request = load(requestId);
						logger.info("Calculating integration preview of pull request #{} in repository '{}'...", 
								request.getNumber(), request.getTargetDepot());
						IntegrationPreview preview = request.getLastIntegrationPreview();
						if (request.isOpen() && (preview == null || preview.isObsolete(request))) {
							String requestHead = request.getLatestUpdate().getHeadCommitHash();
							String targetHead = request.getTarget().getObjectName();
							Depot targetDepot = request.getTargetDepot();
							Git git = request.getTargetDepot().git();
							preview = new IntegrationPreview(targetHead, 
									request.getLatestUpdate().getHeadCommitHash(), request.getIntegrationStrategy(), null);
							request.setLastIntegrationPreview(preview);
							String integrateRef = request.getIntegrateRef();
							if (preview.getIntegrationStrategy() == MERGE_IF_NECESSARY && targetDepot.isAncestor(targetHead, requestHead)
									|| preview.getIntegrationStrategy() == MERGE_WITH_SQUASH && targetDepot.isAncestor(targetHead, requestHead)
											&& git.log(targetHead, requestHead, null, 0, 0, false).size() == 1) {
								preview.setIntegrated(requestHead);
								git.updateRef(integrateRef, requestHead, null, null);
							} else {
								File tempDir = FileUtils.createTempDir();
								try {
									Git tempGit = new Git(tempDir);
									tempGit.clone(git.depotDir().getAbsolutePath(), false, true, true, 
											request.getTargetBranch());
									
									String integrated;

									if (preview.getIntegrationStrategy() == REBASE_TARGET_ONTO_SOURCE) {
										tempGit.updateRef("HEAD", requestHead, null, null);
										tempGit.reset(null, null);
										List<String> cherries = tempGit.listCherries("HEAD", targetHead);
										integrated = tempGit.cherryPick(cherries.toArray(new String[cherries.size()]));
									} else {
										tempGit.updateRef("HEAD", targetHead, null, null);
										tempGit.reset(null, null);
										if (preview.getIntegrationStrategy() == REBASE_SOURCE_ONTO_TARGET) {
											List<String> cherries = tempGit.listCherries("HEAD", requestHead);
											integrated = tempGit.cherryPick(cherries.toArray(new String[cherries.size()]));
										} else if (preview.getIntegrationStrategy() == MERGE_WITH_SQUASH) {
											String commitMessage = request.getTitle() + "\n\n";
											if (request.getDescription() != null)
												commitMessage += request.getDescription() + "\n\n";
											commitMessage += "(squashed commit of pull request #" + request.getNumber() + ")\n";
											integrated = tempGit.squash(requestHead, null, null, commitMessage);
										} else {
											FastForwardMode fastForwardMode;
											if (preview.getIntegrationStrategy() == MERGE_ALWAYS)
												fastForwardMode = FastForwardMode.NO_FF;
											else 
												fastForwardMode = FastForwardMode.FF;
											String commitMessage = "Merge pull request #" + request.getNumber() 
													+ "\n\n" + request.getTitle() + "\n";
											integrated = tempGit.merge(requestHead, fastForwardMode, null, null, commitMessage);
										}
									}
									 
									if (integrated != null) {
										preview.setIntegrated(integrated);
										git.fetch(tempGit, "+HEAD:" + integrateRef);									
									} else {
										git.deleteRef(integrateRef, null, null);
									}
								} finally {
									FileUtils.deleteDir(tempDir);
								}
							}
							persist(request);

							if (request.getStatus() == PENDING_INTEGRATE 
									&& preview.getIntegrated() != null
									&& request.getAssignee() == null) {
								integrate(request, "Integrated automatically by system");
							}
							
							for (PullRequestListener listener: pullRequestListeners)
								listener.onIntegrationPreviewCalculated(request);
						}
						logger.info("Integration preview of pull request #{} in repository '{}' is calculated.", 
								request.getNumber(), request.getTargetDepot());
					} finally {
						integrationPreviewCalculatingRequestIds.remove(requestId);
					}
				}
			} catch (Exception e) {
				logger.error("Error calculating pull request integration preview", e);
			} finally {
				unitOfWork.end();
			}
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IntegrationPreviewTask))
				return false;
			if (this == other)
				return true;
			IntegrationPreviewTask otherRunnable = (IntegrationPreviewTask) other;
			return new EqualsBuilder().append(requestId, otherRunnable.requestId).isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).append(requestId).toHashCode();
		}

	}

	@Override
	public void systemStarted() {
	}

	@Override
	public void systemStopping() {
	}

	@Override
	public void systemStopped() {
	}

	@Transactional
	@Override
	public void onDeleteDepot(Depot depot) {
    	for (PullRequest request: depot.getOutgoingRequests()) {
    		if (!request.getTargetDepot().equals(depot) && request.isOpen())
        		discard(request, "Source repository is deleted.");
    	}
    	
    	Query query = getSession().createQuery("update PullRequest set sourceDepot=null where "
    			+ "sourceDepot=:depot");
    	query.setParameter("depot", depot);
    	query.executeUpdate();
	}
	
	@Override
	public void onRenameDepot(Depot renamedDepot, String oldName) {
	}

	@Transactional
	@Override
	public void onRefUpdate(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		final String branch = GitUtils.ref2branch(refName);
		if (branch != null) {
			DepotAndBranch depotAndBranch = new DepotAndBranch(depot, branch);
			if (!newCommit.equals(ObjectId.zeroId())) {
				/**
				 * Source branch update is key to the logic as it has to create 
				 * pull request update, so we should not postpone it to be executed
				 * in a executor service like target branch update below
				 */
				Criterion criterion = Restrictions.and(ofOpen(), ofSource(depotAndBranch));
				for (PullRequest request: query(EntityCriteria.of(PullRequest.class).add(criterion))) {
					if (depot.getObjectId(request.getBaseCommitHash(), false) != null)
						onSourceBranchUpdate(request, true);
					else
						logger.error("Unable to update pull request #{} due to unexpected source repository.", request.getNumber());
				}
				
				Long depotId = depot.getId();
				afterCommit(new Runnable() {

					@Override
					public void run() {
						unitOfWork.asyncCall(new Runnable() {

							@Override
							public void run() {
								DepotAndBranch depotAndBranch = new DepotAndBranch(depotId, branch);								
								Criterion criterion = Restrictions.and(ofOpen(), ofTarget(depotAndBranch));
								for (PullRequest request: query(EntityCriteria.of(PullRequest.class).add(criterion))) { 
									if (request.getSourceDepot().getObjectId(request.getBaseCommitHash(), false) != null)
										onTargetBranchUpdate(request);
									else
										logger.error("Unable to update pull request #{} due to unexpected target repository.", request.getNumber());
								}
							}
							
						});
					}
					
				});
			} else {
				Criterion criterion = Restrictions.and(
						ofOpen(), 
						Restrictions.or(ofSource(depotAndBranch), ofTarget(depotAndBranch)));
				for (PullRequest request: query(EntityCriteria.of(PullRequest.class).add(criterion))) {
					if (request.getTargetDepot().equals(depot) && request.getTargetBranch().equals(branch)) 
						discard(request, "Target branch is deleted.");
					else
						discard(request, "Source branch is deleted.");
				}
			}
		}
	}

	@Sessional
	@Override
	public PullRequest findOpen(DepotAndBranch target, DepotAndBranch source) {
		return find(EntityCriteria.of(PullRequest.class)
				.add(ofTarget(target)).add(ofSource(source)).add(ofOpen()));
	}

	@Sessional
	@Override
	public Collection<PullRequest> queryOpenTo(DepotAndBranch target, @Nullable Depot sourceDepot) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofTarget(target));

		if (sourceDepot != null)
			criteria.add(Restrictions.eq("sourceDepot", sourceDepot));
		criteria.add(ofOpen());
		return query(criteria);
	}

	@Sessional
	@Override
	public Collection<PullRequest> queryOpenFrom(DepotAndBranch source, @Nullable Depot targetDepot) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofSource(source));
		
		if (targetDepot != null)
			criteria.add(Restrictions.eq("targetDepot", targetDepot));
		criteria.add(ofOpen());
		return query(criteria);
	}

	@Sessional
	@Override
	public Collection<PullRequest> queryOpen(DepotAndBranch sourceOrTarget) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSource(sourceOrTarget), ofTarget(sourceOrTarget)));
		return query(criteria);
	}

	@Transactional
	@Override
	public void checkSanity() {
		logger.info("Checking sanity of pull requests...");
		
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		for (PullRequest request: query(criteria)) {
			Depot sourceDepot = request.getSourceDepot();
			if (sourceDepot == null) {
				discard(request, "Source repository is deleted.");
			} else if (sourceDepot.isValid()) {
				String baseCommitHash = request.getBaseCommitHash();
				
				// only modifies pull request status if source repository is the repository we
				// previously worked with. This avoids disaster of closing all pull requests
				// if repository storage points to a different location by mistake
				if (sourceDepot.getObjectId(baseCommitHash, false) != null) { 
					String sourceHead = request.getSource().getObjectName(false);
					if (sourceHead == null) 
						discard(request, "Source branch is deleted.");
					else 
						onSourceBranchUpdate(request, true);
				} else {
					logger.error("Unable to update pull request #{} due to unexpected source repository", request.getNumber());
				}
			}

			if (request.isOpen()) {
				Depot targetDepot = request.getTargetDepot();
				if (targetDepot.isValid()) {
					String baseCommitHash = request.getBaseCommitHash();
					
					// only modifies pull request status if target repository is the repository we
					// previously worked with. This avoids disaster of closing all pull requests
					// if repository storage points to a different location by mistake
					if (targetDepot.getObjectId(baseCommitHash, false) != null) {
						String targetHead = request.getTarget().getObjectName(false);
						if (targetHead == null)
							discard(request, "Target branch is deleted.");
						else 
							onTargetBranchUpdate(request);
					} else {
						logger.error("Unable to update pull request #{} due to unexpected target repository", request.getNumber());
					}
				}
			}
		}
	}

	@Override
	public void systemStarting() {
	}

	@Override
	public void onTransferDepot(Depot depot, Account oldAccount) {
	}

	@Sessional
	@Override
	public int countOpen(Depot depot) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(PullRequest.CriterionHelper.ofOpen());
		criteria.add(PullRequest.CriterionHelper.ofTargetDepot(depot));
		return count(criteria);
	}

	@Override
	public void onSaveDepot(Depot depot) {
	}

	@Sessional
	@Override
	public PullRequest find(Depot target, long number) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("targetDepot", target));
		criteria.add(Restrictions.eq("number", number));
		
		return find(criteria);
	}

}
