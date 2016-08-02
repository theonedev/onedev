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
import org.apache.shiro.subject.Subject;
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
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.commons.util.match.PatternMatcher;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeCommentRelation;
import com.pmease.gitplex.core.entity.CodeCommentReply;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.pmease.gitplex.core.entity.support.CloseInfo;
import com.pmease.gitplex.core.entity.support.DepotAndBranch;
import com.pmease.gitplex.core.entity.support.IntegrationPolicy;
import com.pmease.gitplex.core.entity.support.IntegrationPreview;
import com.pmease.gitplex.core.entity.support.PullRequestEvent;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.event.PullRequestListener;
import com.pmease.gitplex.core.event.RefUpdated;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.event.codecomment.CodeCommentResolved;
import com.pmease.gitplex.core.event.codecomment.CodeCommentUnresolved;
import com.pmease.gitplex.core.event.depot.DepotDeleted;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.NotificationManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import com.pmease.gitplex.core.manager.ReviewInvitationManager;
import com.pmease.gitplex.core.manager.WorkExecutor;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.core.util.ChildAwareMatcher;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager, 
		PullRequestListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private static final PatternMatcher BRANCH_MATCHER = new ChildAwareMatcher();
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final PullRequestCommentManager commentManager;
	
	private final AccountManager accountManager;
	
	private final UnitOfWork unitOfWork;
	
	private final Provider<Set<PullRequestListener>> pullRequestListenersProvider;
	
	private final ListenerRegistry listenerRegistry;
	
	private final ReviewInvitationManager reviewInvitationManager;
	
	private final Set<Long> integrationPreviewCalculatingRequestIds = new ConcurrentHashSet<>();

	private final WorkExecutor workExecutor;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();

	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager,  
			ReviewInvitationManager reviewInvitationManager, AccountManager accountManager, 
			NotificationManager notificationManager, PullRequestCommentManager commentManager, 
			MarkdownManager markdownManager, WorkExecutor workManager, ListenerRegistry listenerRegistry,
			UnitOfWork unitOfWork, Provider<Set<PullRequestListener>> pullRequestListenersProvider) {
		super(dao);
		
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.reviewInvitationManager = reviewInvitationManager;
		this.commentManager = commentManager;
		this.accountManager = accountManager;
		this.workExecutor = workManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestListenersProvider = pullRequestListenersProvider;
		this.listenerRegistry = listenerRegistry;
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
			for (PullRequestListener listener: pullRequestListenersProvider.get()) {
				listener.onRestoreSourceBranch(request);
			}
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			request.getSource().delete();
			for (PullRequestListener listener: pullRequestListenersProvider.get()) {
				listener.onDeleteSourceBranch(request);
			}
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String commentContent) {
		Preconditions.checkState(!request.isOpen(), "Pull request is alreay opened");
		updateIfNecessary(request, false);
		if (commentContent != null)
			addComment(request, commentContent, false);
		Account user = accountManager.getCurrent();
		if (request.isMerged()) {
			closeAsIntegrated(request, true);
		} else {
			request.setCloseInfo(null);
			request.setLastEvent(PullRequestEvent.REOPENED);
			request.setLastEventDate(new Date());
			request.setLastEventUser(user);
			
			save(request);
			checkAsync(request);
			
			for (PullRequestListener listener: pullRequestListenersProvider.get())
				listener.onReopenRequest(request, user);
		}
	}

	private void addComment(PullRequest request, String commentContent, boolean notifyListeners) {
		PullRequestComment comment = new PullRequestComment();
		comment.setContent(commentContent);
		comment.setRequest(request);
		comment.setUser(accountManager.getCurrent());
		commentManager.save(comment, notifyListeners);
	}
	
	@Transactional
	@Override
 	public void discard(PullRequest request, String commentContent) {
		Account user = accountManager.getCurrent();
		
		if (commentContent != null)
			addComment(request, commentContent, false);

		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(new Date());
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.DISCARDED);
		request.setCloseInfo(closeInfo);
		
		request.setLastEvent(PullRequestEvent.DISCARDED);
		request.setLastEventUser(user);
		request.setLastEventDate(new Date());
		
		dao.persist(request);

		for (PullRequestListener listener: pullRequestListenersProvider.get())
			listener.onDiscardRequest(request, user);
	}
	
	@Transactional
	@Override
	public void integrate(PullRequest request, String commentContent) {
		if (request.getStatus() != PENDING_INTEGRATE)
			throw new IllegalStateException("Gate keeper disallows integration right now.");
	
		IntegrationPreview preview = request.getIntegrationPreview();
		if (preview == null)
			throw new IllegalStateException("Integration preview has not been calculated yet.");

		String integrated = preview.getIntegrated();
		if (integrated == null)
			throw new IllegalStateException("There are integration conflicts.");
		
		Account user = accountManager.getCurrent();

		if (commentContent != null) 
			addComment(request, commentContent, false);
		
		closeAsIntegrated(request, false);

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
			
			updateIfNecessary(request, false);

			Long requestId = request.getId();
			Subject subject = SecurityUtils.getSubject();
			ObjectId newCommitId = integratedCommitId;
			doUnitOfWorkAfterCommitAsync(new Runnable() {

				@Override
				public void run() {
					ThreadContext.bind(subject);
					try {
						PullRequest request = load(requestId);
						request.getSourceDepot().cacheObjectId(request.getSourceRef(), newCommitId);
						listenerRegistry.notify(new RefUpdated(
								request.getSourceDepot(), request.getSourceRef(), requestHeadId, newCommitId));
					} finally {
						ThreadContext.unbindSubject();
					}
				}
				
			});
		}
		
		String targetRef = request.getTargetRef();
		ObjectId targetHeadId = ObjectId.fromString(preview.getTargetHead());
		RefUpdate refUpdate = targetDepot.updateRef(targetRef);
		refUpdate.setRefLogIdent(user.asPerson());
		refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
		refUpdate.setExpectedOldObjectId(targetHeadId);
		refUpdate.setNewObjectId(integratedCommitId);
		GitUtils.updateRef(refUpdate);
		
		Long requestId = request.getId();
		Subject subject = SecurityUtils.getSubject();
		ObjectId newCommitId = integratedCommitId;
		doUnitOfWorkAfterCommitAsync(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					PullRequest request = load(requestId);
					request.getTargetDepot().cacheObjectId(request.getTargetRef(), newCommitId);
					listenerRegistry.notify(new RefUpdated(request.getTargetDepot(), targetRef, 
								ObjectId.fromString(preview.getTargetHead()), newCommitId));
				} finally {
					ThreadContext.unbindSubject();
				}
			}
			
		});
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
			pullRequestUpdateManager.save(update, false);
		}
		
		for (ReviewInvitation invitation: request.getReviewInvitations())
			reviewInvitationManager.save(invitation);

		doAfterCommit(new Runnable() {

			@Override
			public void run() {
				IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
				workExecutor.remove(task);
				workExecutor.execute(task);
			}
			
		});

		for (PullRequestListener listener: pullRequestListenersProvider.get())
			listener.onOpenRequest(request);
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
		
		for (PullRequestListener listener: pullRequestListenersProvider.get())
			listener.onAssignRequest(request, user);
	}
	
	private void onTargetBranchUpdate(PullRequest request) {
		String targetHead = request.getTarget().getObjectName();
		if (request.getLastIntegrationPreview() == null || !request.getLastIntegrationPreview().getTargetHead().equals(targetHead)) {
			if (request.isMerged()) {
				closeAsIntegrated(request, true);
			}
			if (request.isOpen()) {
				IntegrationPreviewTask task = new IntegrationPreviewTask(request.getId());
				workExecutor.remove(task);
				workExecutor.execute(task);
			}
		}
	}

	private void closeAsIntegrated(PullRequest request, boolean dueToMerged) {
		Account user = accountManager.getCurrent();
		
		if (dueToMerged)
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

		for (PullRequestListener listener: pullRequestListenersProvider.get())
			listener.onIntegrateRequest(request, user);
	}

	private void updateIfNecessary(PullRequest request, boolean notifyListeners) {
		if (!request.getLatestUpdate().getHeadCommitHash().equals(request.getSource().getObjectName())) {
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			update.setHeadCommitHash(request.getSource().getObjectName());
			request.addUpdate(update);
			pullRequestUpdateManager.save(update, notifyListeners);
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
		if (request.isOpen()) {
			if (request.isMerged()) {
				closeAsIntegrated(request, true);
			} else {
				if (request.getStatus() == PENDING_UPDATE) {
					for (PullRequestListener listener: pullRequestListenersProvider.get())
						listener.pendingUpdate(request);
				} else if (request.getStatus() == PENDING_INTEGRATE) {
					IntegrationPreview integrationPreview = request.getIntegrationPreview();
					if (integrationPreview != null 
							&& integrationPreview.getIntegrated() != null 
							&& request.getAssignee() == null) {
						integrate(request, "Integrated automatically by system");
					} else {
						for (PullRequestListener listener: pullRequestListenersProvider.get())
							listener.pendingIntegration(request);
					}
				} else if (request.getStatus() == PENDING_APPROVAL) {
					Date now = new Date();
					for (ReviewInvitation invitation: request.getReviewInvitations()) { 
						if (!invitation.getDate().before(now))
							reviewInvitationManager.save(invitation);
					}
					for (PullRequestListener listener: pullRequestListenersProvider.get())
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
			workExecutor.remove(task);
			workExecutor.execute(task);
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
							
							for (PullRequestListener listener: pullRequestListenersProvider.get())
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

	@Transactional
	@Listen
	public void on(DepotDeleted event) {
    	for (PullRequest request: event.getDepot().getOutgoingRequests()) {
    		if (!request.getTargetDepot().equals(event.getDepot()) && request.isOpen())
        		discard(request, "Source repository is deleted.");
    	}
    	
    	Query query = getSession().createQuery("update PullRequest set sourceDepot=null where "
    			+ "sourceDepot=:depot");
    	query.setParameter("depot", event.getDepot());
    	query.executeUpdate();
	}
	
	@Transactional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		if (branch != null) {
			DepotAndBranch depotAndBranch = new DepotAndBranch(event.getDepot(), branch);
			if (!event.getNewCommit().equals(ObjectId.zeroId())) {
				Criterion criterion = Restrictions.and(ofOpen(), ofSource(depotAndBranch));
				for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) {
					if (event.getDepot().getObjectId(request.getBaseCommitHash(), false) != null) {
						updateIfNecessary(request, true);
						if (request.isMerged()) {
							closeAsIntegrated(request, true);
						} else {
							checkAsync(request);
						}
					} else {
						logger.error("Unable to update pull request #{} due to unexpected source repository.", request.getNumber());
					}
				}

				depotAndBranch = new DepotAndBranch(event.getDepot(), branch);								
				criterion = Restrictions.and(ofOpen(), ofTarget(depotAndBranch));
				for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) { 
					if (request.getSourceDepot().getObjectId(request.getBaseCommitHash(), false) != null)
						onTargetBranchUpdate(request);
					else
						logger.error("Unable to update pull request #{} due to unexpected target repository.", request.getNumber());
				}
			} else {
				Criterion criterion = Restrictions.and(
						ofOpen(), 
						Restrictions.or(ofSource(depotAndBranch), ofTarget(depotAndBranch)));
				for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) {
					if (request.getTargetDepot().equals(event.getDepot()) && request.getTargetBranch().equals(branch)) 
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
					if (sourceHead == null) { 
						discard(request, "Source branch is deleted.");
					} else {
						updateIfNecessary(request, false);
						if (request.isMerged()) {
							closeAsIntegrated(request, true);
						} else {
							checkAsync(request);
						}
					}
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

	@Listen
	public void on(CodeCommentReplied event) {
		CodeCommentReply reply = event.getReply();
		for (CodeCommentRelation relation: reply.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			request.setLastEvent(PullRequestEvent.CODE_COMMENT_REPLIED);
			request.setLastEventDate(reply.getDate());
			request.setLastEventUser(reply.getUser());
			request.setLastCodeCommentEventDate(reply.getDate());
			save(request);
		}
	}

	@Listen
	public void on(CodeCommentResolved event) {
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			request.setLastEvent(PullRequestEvent.CODE_COMMENT_RESOLVED);
			request.setLastEventDate(new Date());
			request.setLastEventUser(event.getUser());
			request.setLastCodeCommentEventDate(new Date());
			save(request);
		}
	}

	@Listen
	public void on(CodeCommentUnresolved event) {
		for (CodeCommentRelation relation: event.getComment().getRelations()) {
			PullRequest request = relation.getRequest();
			request.setLastEvent(PullRequestEvent.CODE_COMMENT_UNRESOLVED);
			request.setLastEventDate(new Date());
			request.setLastEventUser(event.getUser());
			request.setLastCodeCommentEventDate(new Date());
			save(request);
		}
	}
	
	@Override
	public void onOpenRequest(PullRequest request) {
	}

	@Override
	public void onReopenRequest(PullRequest request, Account user) {
	}

	@Transactional
	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
		PullRequest request = update.getRequest();
		request.setLastEvent(PullRequestEvent.UPDATED);
		request.setLastEventDate(update.getDate());
		request.setLastEventUser(null);
		save(request);
	}

	@Override
	public void onMentionAccount(PullRequest request, Account account) {
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account account) {
	}

	@Transactional
	@Override
	public void onCommentRequest(PullRequestComment comment) {
		PullRequest request = comment.getRequest();
		request.setLastEvent(PullRequestEvent.COMMENTED);
		request.setLastEventUser(comment.getUser());
		request.setLastEventDate(comment.getDate());
		save(request);
	}

	@Override
	public void onAssignRequest(PullRequest request, Account user) {
	}

	@Override
	public void onVerifyRequest(Verification verification) {
		if (verification.getStatus() != Verification.Status.ONGOING) {
			PullRequest request = verification.getRequest();
			if (verification.getStatus() == Verification.Status.PASSED)
				request.setLastEvent(PullRequestEvent.VERIFICATION_PASSED);
			else
				request.setLastEvent(PullRequestEvent.VERIFICATION_NOT_PASSED);
			request.setLastEventUser(null);
			request.setLastEventDate(new Date());
			save(request);
			
			checkAsync(verification.getRequest());
		}
	}

	@Override
	public void onRestoreSourceBranch(PullRequest request) {
	}

	@Override
	public void onDeleteSourceBranch(PullRequest request) {
	}

	@Override
	public void onIntegrateRequest(PullRequest request, Account user) {
	}

	@Override
	public void onDiscardRequest(PullRequest request, Account user) {
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
	}

	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
	}

	@Override
	public void pendingIntegration(PullRequest request) {
	}

	@Override
	public void pendingUpdate(PullRequest request) {
	}

	@Override
	public void pendingApproval(PullRequest request) {
	}

	@Sessional
	private void checkAsync(PullRequest request) {
		Long requestId = request.getId();
		Subject subject = SecurityUtils.getSubject();
		doUnitOfWorkAfterCommitAsync(new Runnable() {

			@Override
			public void run() {
				try {
			        ThreadContext.bind(subject);
					check(load(requestId));
				} finally {
					ThreadContext.unbindSubject();
				}
			}
	
		});
	}

	@Transactional
	@Override
	public void onReviewRequest(Review review) {
		PullRequest request = review.getUpdate().getRequest();
		if (review.getResult() == Review.Result.APPROVE)
			request.setLastEvent(PullRequestEvent.APPROVED);
		else
			request.setLastEvent(PullRequestEvent.DISAPPROVED);
		request.setLastEventUser(review.getUser());
		request.setLastEventDate(new Date());
		save(request);
		
		checkAsync(review.getUpdate().getRequest());
	}

	@Transactional
	@Override
	public void onDeleteVerification(Verification verification) {
		checkAsync(verification.getRequest());
	}

	@Transactional
	@Override
	public void onDeleteReview(Review review) {
		checkAsync(review.getUpdate().getRequest());
	}

}
