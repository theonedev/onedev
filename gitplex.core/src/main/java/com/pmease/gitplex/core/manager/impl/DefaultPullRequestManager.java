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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.TransactionInterceptor;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.commons.util.match.PatternMatcher;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.component.CloseInfo;
import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;
import com.pmease.gitplex.core.entity.component.IntegrationPreview;
import com.pmease.gitplex.core.entity.component.PullRequestEvent;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.listener.RefListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.VisitInfoManager;
import com.pmease.gitplex.core.manager.WorkExecutor;
import com.pmease.gitplex.core.util.ChildAwareMatcher;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager, 
		DepotListener, RefListener, LifecycleListener, CodeCommentListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private static final PatternMatcher BRANCH_MATCHER = new ChildAwareMatcher();
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final PullRequestCommentManager commentManager;
	
	private final AccountManager accountManager;
	
	private final VisitInfoManager visitInfoManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Provider<Set<PullRequestListener>> listenersProvider;
	
	private final ReviewInvitationManager reviewInvitationManager;
	
	private final Set<Long> integrationPreviewCalculatingRequestIds = new ConcurrentHashSet<>();

	private final WorkExecutor workManager;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();

	@Inject
	public DefaultPullRequestManager(Dao dao, VisitInfoManager visitInfoManager,
			PullRequestUpdateManager pullRequestUpdateManager,  
			ReviewInvitationManager reviewInvitationManager, AccountManager accountManager, 
			NotificationManager notificationManager, PullRequestCommentManager commentManager, 
			MarkdownManager markdownManager, WorkExecutor workManager, 
			UnitOfWork unitOfWork, Provider<Set<PullRequestListener>> listenersProvider) {
		super(dao);
		
		this.visitInfoManager = visitInfoManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.reviewInvitationManager = reviewInvitationManager;
		this.commentManager = commentManager;
		this.accountManager = accountManager;
		this.workManager = workManager;
		this.unitOfWork = unitOfWork;
		this.listenersProvider = listenersProvider;
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

	@Transactional
	@Override
	public void restoreSourceBranch(PullRequest request) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null);
		if (request.getSource().getObjectName(false) == null) {
			RevCommit latestCommit = request.getLatestUpdate().getHeadCommit();
			try {
				request.getSourceDepot().git().branchCreate().setName(request.getSourceBranch()).setStartPoint(latestCommit).call();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
			request.getSourceDepot().cacheObjectId(request.getSourceBranch(), latestCommit.copy());
			if (TransactionInterceptor.isInitiating()) {
				for (PullRequestListener listener: listenersProvider.get()) {
					listener.onRestoreSourceBranch(request);
				}
			}
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			request.getSource().delete();
			if (TransactionInterceptor.isInitiating()) {
				for (PullRequestListener listener: listenersProvider.get()) {
					listener.onDeleteSourceBranch(request);
				}
			}
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String comment) {
		Preconditions.checkState(!request.isOpen(), "Pull request is alreay opened");
		
		Account user = accountManager.getCurrent();
		
		request.setCloseInfo(null);
		request.setLastEvent(PullRequestEvent.REOPENED);
		request.setLastEventDate(new Date());
		request.setLastEventUser(user);
		
		dao.persist(request);
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setRequest(request);
			requestComment.setUser(user);
			commentManager.save(requestComment);
		}

		onSourceBranchUpdate(request);
		
		if (request.isOpen()) {
			for (PullRequestListener listener: listenersProvider.get())
				listener.onReopenRequest(request, user, comment);
		}
	}

	@Transactional
	@Override
 	public void discard(PullRequest request, final String comment) {
		Account user = accountManager.getCurrent();
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setRequest(request);
			requestComment.setUser(user);
			
			commentManager.save(requestComment);
		}

		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(new Date());
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.DISCARDED);
		request.setCloseInfo(closeInfo);
		
		request.setLastEvent(PullRequestEvent.DISCARDED);
		request.setLastEventUser(user);
		request.setLastEventDate(new Date());
		
		dao.persist(request);

		for (PullRequestListener listener: listenersProvider.get())
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
		
		Account user = accountManager.getCurrent();

		ObjectId integratedCommitId = ObjectId.fromString(integrated);
		RevCommit integratedCommit = request.getTargetDepot().getRevCommit(integratedCommitId);
		
		Depot targetDepot = request.getTargetDepot();
		IntegrationStrategy strategy = request.getIntegrationStrategy();
		if ((strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY || strategy == MERGE_WITH_SQUASH) 
				&& !preview.getIntegrated().equals(preview.getRequestHead()) 
				&& !integratedCommit.getFullMessage().equals(request.getCommitMessage())) {
			try (	RevWalk revWalk = new RevWalk(targetDepot.getRepository());
					ObjectInserter inserter = targetDepot.getRepository().newObjectInserter()) {
				RevCommit commit = revWalk.parseCommit(ObjectId.fromString(integrated));
		        CommitBuilder newCommit = new CommitBuilder();
		        newCommit.setAuthor(commit.getAuthorIdent());
		        newCommit.setCommitter(user.asPerson());
		        newCommit.setMessage(request.getCommitMessage());
		        newCommit.setTreeId(commit.getTree());
		        newCommit.setParentIds(commit.getParents());
		        integrated = inserter.insert(newCommit).name();
		        inserter.flush();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}
		integratedCommitId = ObjectId.fromString(integrated);
		if (strategy == REBASE_SOURCE_ONTO_TARGET || strategy == MERGE_WITH_SQUASH) {
			Depot sourceDepot = request.getSourceDepot();
			RefUpdate refUpdate = sourceDepot.updateRef(request.getSourceRef());
			refUpdate.setNewObjectId(integratedCommitId);
			ObjectId requestHeadId = ObjectId.fromString(preview.getRequestHead());
			refUpdate.setExpectedOldObjectId(requestHeadId);
			refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
			GitUtils.updateRef(refUpdate);
			
			sourceDepot.cacheObjectId(request.getSourceRef(), integratedCommitId);
			onRefUpdate(sourceDepot, request.getSourceRef(), requestHeadId, integratedCommitId);
		}
		
		String targetRef = request.getTargetRef();
		ObjectId targetHeadId = ObjectId.fromString(preview.getTargetHead());
		RefUpdate refUpdate = targetDepot.updateRef(targetRef);
		refUpdate.setRefLogIdent(user.asPerson());
		refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
		refUpdate.setExpectedOldObjectId(targetHeadId);
		refUpdate.setNewObjectId(integratedCommitId);
		GitUtils.updateRef(refUpdate);
		
		targetDepot.cacheObjectId(request.getTargetRef(), integratedCommitId);
		onRefUpdate(targetDepot, targetRef, ObjectId.fromString(preview.getTargetHead()), integratedCommitId);
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setContent(comment);
			requestComment.setRequest(request);
			requestComment.setUser(user);
			commentManager.save(requestComment);
		}

		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(new Date());
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
		request.setCloseInfo(closeInfo);
		
		request.setLastEvent(PullRequestEvent.INTEGRATED);
		request.setLastEventUser(user);
		request.setLastEventDate(new Date());
		
		dao.persist(request);
		
		for (PullRequestListener listener: listenersProvider.get())
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
	public void open(PullRequest request) {
		request.setNumber(getNextNumber(request.getTargetDepot()));
		request.setLastEvent(PullRequestEvent.OPENED);
		request.setLastEventDate(request.getSubmitDate());
		request.setLastEventUser(request.getSubmitter());
		dao.persist(request);
		
		RefUpdate refUpdate = request.getTargetDepot().updateRef(request.getBaseRef());
		refUpdate.setNewObjectId(ObjectId.fromString(request.getBaseCommitHash()));
		GitUtils.updateRef(refUpdate);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			pullRequestUpdateManager.save(update);
		}
		
		for (ReviewInvitation invitation: request.getReviewInvitations())
			reviewInvitationManager.save(invitation);

		for (PullRequestListener listener: listenersProvider.get())
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
	public void changeAssignee(PullRequest request) {
		Account user = accountManager.getCurrent();
		request.setLastEvent(PullRequestEvent.ASSIGNED);
		request.setLastEventUser(user);
		request.setLastEventDate(new Date());
		dao.persist(request);
		
		if (TransactionInterceptor.isInitiating()) {
			for (PullRequestListener listener: listenersProvider.get())
				listener.onAssignRequest(request, user);
		}
	}
	
	@Sessional
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
		if (request.getTargetDepot().isMergedInto(request.getLatestUpdate().getHeadCommitHash(), request.getTarget().getObjectName())) {
			Account user = accountManager.getCurrent();
			
			request.setLastIntegrationPreview(null);
			CloseInfo closeInfo = new CloseInfo();
			closeInfo.setCloseDate(new Date());
			closeInfo.setClosedBy(user);
			closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
			request.setCloseInfo(closeInfo);
			
			request.setLastEvent(PullRequestEvent.INTEGRATED);
			request.setLastEventUser(user);
			request.setLastEventDate(new Date());
			
			dao.persist(request);

			if (TransactionInterceptor.isInitiating()) {
				for (PullRequestListener listener: listenersProvider.get())
					listener.onIntegrateRequest(request, user, null);
			}
		} 
	}

	@Sessional
	@Override
	public void onSourceBranchUpdate(PullRequest request) {
		if (!request.getLatestUpdate().getHeadCommitHash().equals(request.getSource().getObjectName())) {
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			update.setHeadCommitHash(request.getSource().getObjectName());
			request.addUpdate(update);
			pullRequestUpdateManager.save(update);
			closeIfMerged(request);

			if (request.isOpen()) {
				Long requestId = request.getId();
				afterCommit(new Runnable() {

					@Override
					public void run() {
						unitOfWork.asyncCall(new Runnable() {

							@Override
							public void run() {
								try {
							        ThreadContext.bind(accountManager.getRoot().asSubject());
									check(load(requestId));
								} finally {
									ThreadContext.unbindSubject();
								}
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
	@Sessional
	@Override
	public void check(PullRequest request) {
		Date now = new Date();
		if (request.isOpen()) {
			closeIfMerged(request);
			if (request.isOpen()) {
				if (request.getStatus() == PENDING_UPDATE) {
					for (PullRequestListener listener: listenersProvider.get())
						listener.pendingUpdate(request);
				} else if (request.getStatus() == PENDING_INTEGRATE) {
					IntegrationPreview integrationPreview = request.getIntegrationPreview();
					if (integrationPreview != null 
							&& integrationPreview.getIntegrated() != null 
							&& request.getAssignee() == null) {
						integrate(request, "Integrated automatically by system");
					} else {
						for (PullRequestListener listener: listenersProvider.get())
							listener.pendingIntegration(request);
					}
				} else if (request.getStatus() == PENDING_APPROVAL) {
					for (ReviewInvitation invitation: request.getReviewInvitations()) { 
						if (!invitation.getDate().before(now))
							reviewInvitationManager.save(invitation);
					}
					for (PullRequestListener listener: listenersProvider.get())
						listener.pendingApproval(request);
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
							preview = new IntegrationPreview(targetHead, 
									request.getLatestUpdate().getHeadCommitHash(), request.getIntegrationStrategy(), null);
							request.setLastIntegrationPreview(preview);
							String integrateRef = request.getIntegrateRef();
							if (preview.getIntegrationStrategy() == MERGE_IF_NECESSARY && targetDepot.isMergedInto(targetHead, requestHead)) {
								preview.setIntegrated(requestHead);
								RefUpdate refUpdate = targetDepot.updateRef(integrateRef);
								refUpdate.setNewObjectId(ObjectId.fromString(requestHead));
								GitUtils.updateRef(refUpdate);
							} else {
								PersonIdent user = accountManager.getRoot().asPerson();
								ObjectId integrated;
								ObjectId requestHeadId = ObjectId.fromString(requestHead);
								ObjectId targetHeadId = ObjectId.fromString(targetHead);
								if (preview.getIntegrationStrategy() == REBASE_TARGET_ONTO_SOURCE) {
									integrated = GitUtils.rebase(targetDepot.getRepository(), targetHeadId, requestHeadId, user);
								} else if (preview.getIntegrationStrategy() == REBASE_SOURCE_ONTO_TARGET) {
									integrated = GitUtils.rebase(targetDepot.getRepository(), requestHeadId, targetHeadId, user);
								} else if (preview.getIntegrationStrategy() == MERGE_WITH_SQUASH) {
									integrated = GitUtils.merge(targetDepot.getRepository(), 
											requestHeadId, targetHeadId, true, user, request.getCommitMessage());
								} else {
									integrated = GitUtils.merge(targetDepot.getRepository(), 
											requestHeadId, targetHeadId, false, user, request.getCommitMessage());
								} 
								
								if (integrated != null) {
									preview.setIntegrated(integrated.name());
									RefUpdate refUpdate = targetDepot.updateRef(integrateRef);
									refUpdate.setNewObjectId(integrated);
									GitUtils.updateRef(refUpdate);
								} else {
									RefUpdate refUpdate = targetDepot.updateRef(integrateRef);
									GitUtils.deleteRef(refUpdate);
								}
							}
							dao.persist(request);

							if (request.getStatus() == PENDING_INTEGRATE 
									&& preview.getIntegrated() != null
									&& request.getAssignee() == null) {
								integrate(request, "Integrated automatically by GitPlex");
							}
							
							for (PullRequestListener listener: listenersProvider.get())
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

	@Sessional
	@Override
	public void onRefUpdate(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		String branch = GitUtils.ref2branch(refName);
		if (branch != null) {
			DepotAndBranch depotAndBranch = new DepotAndBranch(depot, branch);
			if (!newCommit.equals(ObjectId.zeroId())) {
				/**
				 * Source branch update is key to the logic as it has to create 
				 * pull request update, so we should not postpone it to be executed
				 * in a executor service like target branch update below
				 */
				Criterion criterion = Restrictions.and(ofOpen(), ofSource(depotAndBranch));
				for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) {
					if (depot.getObjectId(request.getBaseCommitHash(), false) != null)
						onSourceBranchUpdate(request);
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
								try {
							        ThreadContext.bind(accountManager.getRoot().asSubject());
									DepotAndBranch depotAndBranch = new DepotAndBranch(depotId, branch);								
									Criterion criterion = Restrictions.and(ofOpen(), ofTarget(depotAndBranch));
									for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) { 
										if (request.getSourceDepot().getObjectId(request.getBaseCommitHash(), false) != null)
											onTargetBranchUpdate(request);
										else
											logger.error("Unable to update pull request #{} due to unexpected target repository.", request.getNumber());
									}
								} finally {
									ThreadContext.unbindSubject();
								}
							}
							
						});
					}
					
				});
			} else {
				Criterion criterion = Restrictions.and(
						ofOpen(), 
						Restrictions.or(ofSource(depotAndBranch), ofTarget(depotAndBranch)));
				for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) {
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
	public Collection<PullRequest> findAllOpenTo(DepotAndBranch target, @Nullable Depot sourceDepot) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofTarget(target));

		if (sourceDepot != null)
			criteria.add(Restrictions.eq("sourceDepot", sourceDepot));
		criteria.add(ofOpen());
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<PullRequest> findAllOpenFrom(DepotAndBranch source, @Nullable Depot targetDepot) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofSource(source));
		
		if (targetDepot != null)
			criteria.add(Restrictions.eq("targetDepot", targetDepot));
		criteria.add(ofOpen());
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<PullRequest> findAllOpen(DepotAndBranch sourceOrTarget) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSource(sourceOrTarget), ofTarget(sourceOrTarget)));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public void checkSanity() {
		logger.info("Checking sanity of pull requests...");
		
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		for (PullRequest request: findAll(criteria)) {
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
						onSourceBranchUpdate(request);
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

	@Sessional
	@Override
	public PullRequest find(Depot target, long number) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("targetDepot", target));
		criteria.add(Restrictions.eq("number", number));
		return find(criteria);
	}

	@Sessional
	@Override
	public PullRequest find(String uuid) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("uuid", uuid));
		return find(criteria);
	}

	@Override
	public void onComment(CodeComment comment) {
	}

	@Override
	public void onReplyComment(CodeCommentReply reply) {
		for (CodeCommentRelation relation: reply.getComment().getRequestRelations()) {
			PullRequest request = relation.getRequest();
			request.setLastEvent(PullRequestEvent.CODE_COMMENT_REPLIED);
			request.setLastEventDate(reply.getDate());
			request.setLastEventUser(reply.getUser());
			request.setLastCodeCommentEventDate(reply.getDate());
			save(request);
			
			visitInfoManager.visit(reply.getUser(), request);
		}
	}

	@Override
	public void onToggleResolve(CodeComment comment, Account user) {
		for (CodeCommentRelation relation: comment.getRequestRelations()) {
			PullRequest request = relation.getRequest();
			if (comment.isResolved())
				request.setLastEvent(PullRequestEvent.CODE_COMMENT_RESOLVED);
			else
				request.setLastEvent(PullRequestEvent.CODE_COMMENT_UNRESOLVED);
			request.setLastEventDate(new Date());
			request.setLastEventUser(user);
			request.setLastCodeCommentEventDate(new Date());
			save(request);
			
			visitInfoManager.visit(user, request);
		}
	}

}
