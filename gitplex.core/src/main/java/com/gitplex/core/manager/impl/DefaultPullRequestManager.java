package com.gitplex.core.manager.impl;

import static com.gitplex.core.entity.PullRequest.CriterionHelper.ofOpen;
import static com.gitplex.core.entity.PullRequest.CriterionHelper.ofSource;
import static com.gitplex.core.entity.PullRequest.CriterionHelper.ofSourceDepot;
import static com.gitplex.core.entity.PullRequest.CriterionHelper.ofSubmitter;
import static com.gitplex.core.entity.PullRequest.CriterionHelper.ofTarget;
import static com.gitplex.core.entity.PullRequest.CriterionHelper.ofTargetDepot;
import static com.gitplex.core.entity.PullRequest.IntegrationStrategy.MERGE_ALWAYS;
import static com.gitplex.core.entity.PullRequest.IntegrationStrategy.MERGE_IF_NECESSARY;
import static com.gitplex.core.entity.PullRequest.IntegrationStrategy.MERGE_WITH_SQUASH;
import static com.gitplex.core.entity.PullRequest.IntegrationStrategy.REBASE_SOURCE_ONTO_TARGET;
import static com.gitplex.core.entity.PullRequest.IntegrationStrategy.REBASE_TARGET_ONTO_SOURCE;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestReviewInvitation;
import com.gitplex.core.entity.PullRequestStatusChange;
import com.gitplex.core.entity.PullRequestUpdate;
import com.gitplex.core.entity.PullRequest.IntegrationStrategy;
import com.gitplex.core.entity.PullRequestStatusChange.Type;
import com.gitplex.core.entity.support.CloseInfo;
import com.gitplex.core.entity.support.DepotAndBranch;
import com.gitplex.core.entity.support.IntegrationPolicy;
import com.gitplex.core.entity.support.IntegrationPreview;
import com.gitplex.core.event.RefUpdated;
import com.gitplex.core.event.depot.DepotDeleted;
import com.gitplex.core.event.pullrequest.IntegrationPreviewCalculated;
import com.gitplex.core.event.pullrequest.PullRequestOpened;
import com.gitplex.core.event.pullrequest.PullRequestPendingApproval;
import com.gitplex.core.event.pullrequest.PullRequestPendingIntegration;
import com.gitplex.core.event.pullrequest.PullRequestPendingUpdate;
import com.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.core.gatekeeper.checkresult.Failed;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.core.gatekeeper.checkresult.Pending;
import com.gitplex.core.manager.AccountManager;
import com.gitplex.core.manager.BatchWorkManager;
import com.gitplex.core.manager.PullRequestManager;
import com.gitplex.core.manager.PullRequestReviewInvitationManager;
import com.gitplex.core.manager.PullRequestStatusChangeManager;
import com.gitplex.core.manager.PullRequestUpdateManager;
import com.gitplex.core.manager.support.BatchWorker;
import com.gitplex.core.security.SecurityUtils;
import com.gitplex.core.util.ChildAwareMatcher;
import com.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;
import com.gitplex.core.util.includeexclude.IncludeExcludeUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.UnitOfWork;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.commons.loader.Listen;
import com.gitplex.commons.loader.ListenerRegistry;
import com.gitplex.commons.markdown.MarkdownManager;
import com.gitplex.commons.util.concurrent.Prioritized;
import com.gitplex.commons.util.match.PatternMatcher;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private static final PatternMatcher BRANCH_MATCHER = new ChildAwareMatcher();
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final AccountManager accountManager;
	
	private final UnitOfWork unitOfWork;
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestReviewInvitationManager reviewInvitationManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager,  
			PullRequestReviewInvitationManager reviewInvitationManager, AccountManager accountManager, 
			MarkdownManager markdownManager, BatchWorkManager batchWorkManager, 
			ListenerRegistry listenerRegistry, UnitOfWork unitOfWork, 
			PullRequestStatusChangeManager pullRequestStatusChangeManager) {
		super(dao);
		
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.reviewInvitationManager = reviewInvitationManager;
		this.accountManager = accountManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.listenerRegistry = listenerRegistry;
		this.pullRequestStatusChangeManager = pullRequestStatusChangeManager;
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
	public void restoreSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null);
		if (request.getSource().getObjectName(false) == null) {
			RevCommit latestCommit = request.getHeadCommit();
			try {
				request.getSourceDepot().git().branchCreate().setName(request.getSourceBranch()).setStartPoint(latestCommit).call();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
			request.getSourceDepot().cacheObjectId(request.getSourceBranch(), latestCommit.copy());
			
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setNote(note);
			statusChange.setRequest(request);
			statusChange.setType(Type.SOURCE_BRANCH_RESTORED);
			statusChange.setUser(accountManager.getCurrent());
			pullRequestStatusChangeManager.save(statusChange);
			
			request.setLastEvent(statusChange);
			save(request);
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceDepot() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			request.getSource().delete();
			
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setNote(note);
			statusChange.setRequest(request);
			statusChange.setType(Type.SOURCE_BRANCH_DELETED);
			statusChange.setUser(accountManager.getCurrent());
			pullRequestStatusChangeManager.save(statusChange);
			
			request.setLastEvent(statusChange);
			save(request);
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String note) {
		Account user = accountManager.getCurrent();
		request.setCloseInfo(null);

		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setNote(note);
		statusChange.setRequest(request);
		statusChange.setType(Type.REOPENED);
		statusChange.setUser(user);
		pullRequestStatusChangeManager.save(statusChange);

		request.setLastEvent(statusChange);
		save(request);
		checkAsync(request);
	}

	@Transactional
	@Override
 	public void discard(PullRequest request, String note) {
		Account user = accountManager.getCurrent();
		Date date = new Date();
		
		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(date);
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.DISCARDED);
		request.setCloseInfo(closeInfo);
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(date);
		statusChange.setNote(note);
		statusChange.setRequest(request);
		statusChange.setType(Type.DISCARDED);
		statusChange.setUser(user);
		pullRequestStatusChangeManager.save(statusChange);

		request.setLastEvent(statusChange);
		save(request);
	}
	
	@Transactional
	@Override
	public void integrate(PullRequest request, String note) {
		IntegrationPreview preview = request.getIntegrationPreview();
		if (preview == null)
			throw new IllegalStateException("Integration preview has not been calculated yet.");

		String integrated = preview.getIntegrated();
		if (integrated == null)
			throw new IllegalStateException("There are integration conflicts.");
		
		Account user = accountManager.getCurrent();

		ObjectId integratedId = ObjectId.fromString(integrated);
		RevCommit integratedCommit = request.getTargetDepot().getRevCommit(integratedId);
		
		Depot targetDepot = request.getTargetDepot();
		IntegrationStrategy strategy = request.getIntegrationStrategy();
		if ((strategy == MERGE_ALWAYS || strategy == MERGE_IF_NECESSARY || strategy == MERGE_WITH_SQUASH) 
				&& !preview.getIntegrated().equals(preview.getRequestHead()) 
				&& !integratedCommit.getFullMessage().equals(request.getCommitMessage())) {
			try (	RevWalk revWalk = new RevWalk(targetDepot.getRepository());
					ObjectInserter inserter = targetDepot.getRepository().newObjectInserter()) {
		        CommitBuilder newCommit = new CommitBuilder();
		        newCommit.setAuthor(integratedCommit.getAuthorIdent());
		        newCommit.setCommitter(user.asPerson());
		        newCommit.setMessage(request.getCommitMessage());
		        newCommit.setTreeId(integratedCommit.getTree());
		        newCommit.setParentIds(integratedCommit.getParents());
		        integratedId = inserter.insert(newCommit);
		        integrated = integratedId.name();
		        inserter.flush();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
	        preview = new IntegrationPreview(preview.getTargetHead(), preview.getRequestHead(), 
	        		preview.getIntegrationStrategy(), integrated);
	        request.setLastIntegrationPreview(preview);
			RefUpdate refUpdate = targetDepot.updateRef(request.getIntegrateRef());
			refUpdate.setNewObjectId(integratedId);
			GitUtils.updateRef(refUpdate);
		}
		
		closeAsIntegrated(request, false, note);

		if (strategy == REBASE_SOURCE_ONTO_TARGET || strategy == MERGE_WITH_SQUASH) {
			Depot sourceDepot = request.getSourceDepot();
			if (!sourceDepot.equals(targetDepot)) {
				try {
					sourceDepot.git().fetch()
						.setRemote(targetDepot.getDirectory().getAbsolutePath())
						.setRefSpecs(new RefSpec(request.getIntegrateRef()))
						.call();
				} catch (GitAPIException e) {
					throw new RuntimeException(e);
				}
			} 
			RefUpdate refUpdate = sourceDepot.updateRef(request.getSourceRef());
			refUpdate.setNewObjectId(integratedId);
			ObjectId requestHeadId = ObjectId.fromString(preview.getRequestHead());
			refUpdate.setExpectedOldObjectId(requestHeadId);
			refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
			GitUtils.updateRef(refUpdate);

			checkUpdate(request, false);

			Long requestId = request.getId();
			ObjectId newSourceHeadId = integratedId;
			Subject subject = SecurityUtils.getSubject();
			doUnitOfWorkAsyncAfterCommit(new Runnable() {

				@Override
				public void run() {
					ThreadContext.bind(subject);
					try {
						PullRequest request = load(requestId);
						request.getSourceDepot().cacheObjectId(request.getSourceRef(), newSourceHeadId);
						listenerRegistry.post(new RefUpdated(
								request.getSourceDepot(), request.getSourceRef(), requestHeadId, newSourceHeadId));
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
		refUpdate.setNewObjectId(integratedId);
		GitUtils.updateRef(refUpdate);
		
		Long requestId = request.getId();
		Subject subject = SecurityUtils.getSubject();
		ObjectId newTargetHeadId = integratedId;
		doUnitOfWorkAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					PullRequest request = load(requestId);
					request.getTargetDepot().cacheObjectId(request.getTargetRef(), newTargetHeadId);
					listenerRegistry.post(new RefUpdated(request.getTargetDepot(), targetRef, 
								targetHeadId, newTargetHeadId));
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
		dao.persist(request);
		
		RefUpdate refUpdate = request.getTargetDepot().updateRef(request.getBaseRef());
		refUpdate.setNewObjectId(ObjectId.fromString(request.getBaseCommitHash()));
		GitUtils.updateRef(refUpdate);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			pullRequestUpdateManager.save(update, false);
		}
		
		for (PullRequestReviewInvitation invitation: request.getReviewInvitations())
			reviewInvitationManager.save(invitation);

		checkAsync(request);
		
		listenerRegistry.post(new PullRequestOpened(request));
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
		if (request.isOpen()) {
			String note = request.getAssignee().getDisplayName() + " is expected to integrate the pull request";

			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setNote(note);
			statusChange.setRequest(request);
			statusChange.setType(Type.ASSIGNED);
			statusChange.setUser(accountManager.getCurrent());
			pullRequestStatusChangeManager.save(statusChange);

			request.setLastEvent(statusChange);
		}
		save(request);
	}

	private void closeAsIntegrated(PullRequest request, boolean dueToMerged, String note) {
		Account user = accountManager.getCurrent();
		Date date = new Date();
		
		if (dueToMerged)
			request.setLastIntegrationPreview(null);
		
		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(date);
		closeInfo.setClosedBy(user);
		closeInfo.setCloseStatus(CloseInfo.Status.INTEGRATED);
		request.setCloseInfo(closeInfo);
		
		if (dueToMerged)
			note = "Source branch is already merged to target branch by some one";
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(date);
		statusChange.setNote(note);
		statusChange.setRequest(request);
		statusChange.setType(Type.INTEGRATED);
		statusChange.setUser(user);
		pullRequestStatusChangeManager.save(statusChange);
		request.setLastEvent(statusChange);
		
		save(request);
	}

	private void checkUpdate(PullRequest request, boolean independent) {
		if (!request.getHeadCommitHash().equals(request.getSource().getObjectName())) {
			PullRequestUpdate update = new PullRequestUpdate();
			update.setRequest(request);
			update.setHeadCommitHash(request.getSource().getObjectName());
			ObjectId mergeBase = GitUtils.getMergeBase(
					request.getTargetDepot().getRepository(), request.getTarget().getObjectId(), 
					request.getSourceDepot().getRepository(), request.getSource().getObjectId(), 
					GitUtils.branch2ref(request.getSourceBranch()));
			update.setMergeCommitHash(mergeBase.name());
			request.addUpdate(update);
			pullRequestUpdateManager.save(update, independent);
		}
	}

	@Transactional
	@Override
	public void check(PullRequest request) {
		if (request.isOpen()) {
			if (request.getSourceDepot() == null) {
				discard(request, "Source repository no longer exists");
			} else if (request.getSource().getObjectId(false) == null) {
				discard(request, "Source branch no longer exists");
			} else if (request.getTarget().getObjectId(false) == null) {
				discard(request, "Target branch no longer exists");
			} else {
				checkUpdate(request, true);
				if (request.isMerged()) {
					closeAsIntegrated(request, true, null);
				} else {
					IntegrationPreview integrationPreview = request.getIntegrationPreview();
					Date timeBeforeCheck = new Date();
					GateCheckResult result = request.checkGates(false);					
					if (result instanceof Pending) { 
						for (PullRequestReviewInvitation invitation: request.getReviewInvitations()) { 
							if (invitation.getDate().getTime()>=timeBeforeCheck.getTime())
								reviewInvitationManager.save(invitation);
						}
						listenerRegistry.post(new PullRequestPendingApproval(request));
					} else if (result instanceof Failed) { 
						listenerRegistry.post(new PullRequestPendingUpdate(request));
					} else if (integrationPreview != null 
								&& integrationPreview.getIntegrated() != null 
								&& request.getAssignee() == null) {
						integrate(request, "Integrated automatically by GitPlex");
					} else {
						listenerRegistry.post(new PullRequestPendingIntegration(request));
					}
				}
			}
		}
	}

	@Override
	public IntegrationPreview previewIntegration(PullRequest request) {
		IntegrationPreview preview = request.getLastIntegrationPreview();
		if (request.isOpen() && !request.isMerged() && (preview == null || preview.isObsolete(request))) {
			int priority = RequestCycle.get() != null?UI_PREVIEW_PRIORITY:BACKEND_PREVIEW_PRIORITY;			
			if (dao.getSession().getTransaction().getStatus() == TransactionStatus.ACTIVE) {
				doAfterCommit(new Runnable() {

					@Override
					public void run() {
						batchWorkManager.submit(getIntegrationPreviewer(request), new Prioritized(priority));
					}
					
				});
			} else {
				batchWorkManager.submit(getIntegrationPreviewer(request), new Prioritized(priority));
			}
			return null;
		} else {
			return preview;
		}
	}
	
	private BatchWorker getIntegrationPreviewer(PullRequest request) {
		Long requestId = request.getId();
		return new BatchWorker("request-" + requestId + "-previewIntegration", 1) {

			@Override
			public void doWork(Collection<Prioritized> works) {
				Preconditions.checkState(works.size() == 1);
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						PullRequest request = load(requestId);
						try {
							IntegrationPreview preview = request.getLastIntegrationPreview();
							if (request.isOpen() && !request.isMerged() && (preview == null || preview.isObsolete(request))) {
								logger.info("Calculating integration preview of pull request #{} in repository '{}'...", 
										request.getNumber(), request.getTargetDepot());
								String requestHead = request.getHeadCommitHash();
								String targetHead = request.getTarget().getObjectName();
								Depot targetDepot = request.getTargetDepot();
								preview = new IntegrationPreview(targetHead, requestHead, request.getIntegrationStrategy(), null);
								request.setLastIntegrationPreview(preview);
								String integrateRef = request.getIntegrateRef();
								ObjectId requestHeadId = ObjectId.fromString(requestHead);
								ObjectId targetHeadId = ObjectId.fromString(targetHead);
								if (preview.getIntegrationStrategy() == MERGE_IF_NECESSARY 
										&& GitUtils.isMergedInto(targetDepot.getRepository(), targetHeadId, requestHeadId)) {
									preview.setIntegrated(requestHead);
									RefUpdate refUpdate = targetDepot.updateRef(integrateRef);
									refUpdate.setNewObjectId(ObjectId.fromString(requestHead));
									GitUtils.updateRef(refUpdate);
								} else {
									PersonIdent user = accountManager.getRoot().asPerson();
									ObjectId integrated;
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

								listenerRegistry.post(new IntegrationPreviewCalculated(request));
								logger.info("Integration preview of pull request #{} in repository '{}' is calculated.", 
										request.getNumber(), request.getTargetDepot());						
							}
						} catch (Exception e) {
							logger.error("Error calculating pull request integration preview", e);
						}
						return null;
					}
					
				});
			}
			
		};
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
			Criterion criterion = Restrictions.and(
					ofOpen(), 
					Restrictions.or(ofSource(depotAndBranch), ofTarget(depotAndBranch)));
			for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) {
				check(request);
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
	public PullRequest findLatest(Depot depot, Account submitter) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSourceDepot(depot), ofTargetDepot(depot)));
		criteria.add(ofSubmitter(submitter));
		criteria.addOrder(Order.desc("id"));
		return find(criteria);
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
	public void on(PullRequestStatusChangeEvent event) {
		Type type =  event.getStatusChange().getType();
		if (type == Type.APPROVED || type == Type.DISAPPROVED || type == Type.REVIEW_DELETED
				|| type == Type.VERIFICATION_DELETED || type == Type.VERIFICATION_SUCCEEDED
				|| type == Type.VERIFICATION_FAILED) {
			checkAsync(event.getRequest());
		}
	}
	
	@Listen
	public void on(IntegrationPreviewCalculated event) {
		checkAsync(event.getRequest());
	}
	
	private Runnable newCheckRunnable(Long requestId, Subject subject) {
		return new Runnable() {
			
			@Override
			public void run() {
				try {
			        ThreadContext.bind(subject);
					check(load(requestId));
				} catch (Exception e) {
					logger.error("Error checking pull request", e);
				} finally {
					ThreadContext.unbindSubject();
				}
			}
	
		};		
	}
	
	@Sessional
	protected void checkAsync(PullRequest request) {
		Long requestId = request.getId();
		Subject subject = SecurityUtils.getSubject();
		if (dao.getSession().getTransaction().getStatus() == TransactionStatus.ACTIVE) {
			doUnitOfWorkAsyncAfterCommit(newCheckRunnable(requestId, subject));
		} else {
			unitOfWork.doAsync(newCheckRunnable(requestId, subject));
		}
	}

}
