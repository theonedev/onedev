package com.gitplex.server.manager.impl;

import static com.gitplex.server.model.PullRequest.CriterionHelper.ofOpen;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofSource;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofSourceDepot;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofSubmitter;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofTarget;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofTargetDepot;
import static com.gitplex.server.model.support.MergeStrategy.ALWAYS_MERGE;
import static com.gitplex.server.model.support.MergeStrategy.MERGE_IF_NECESSARY;
import static com.gitplex.server.model.support.MergeStrategy.REBASE_MERGE;
import static com.gitplex.server.model.support.MergeStrategy.SQUASH_MERGE;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.GitPlex;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.event.depot.DepotDeleted;
import com.gitplex.server.event.pullrequest.MergePreviewCalculated;
import com.gitplex.server.event.pullrequest.PullRequestOpened;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.BatchWorkManager;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.manager.PullRequestUpdateManager;
import com.gitplex.server.manager.ReviewInvitationManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestReview;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestStatusChange.Type;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.model.support.CloseInfo;
import com.gitplex.server.model.support.DepotAndBranch;
import com.gitplex.server.model.support.MergePreview;
import com.gitplex.server.model.support.MergeStrategy;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.BatchWorker;
import com.gitplex.server.util.ReviewStatus;
import com.gitplex.server.util.concurrent.Prioritized;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final AccountManager accountManager;
	
	private final UnitOfWork unitOfWork;
	
	private final ListenerRegistry listenerRegistry;
	
	private final ReviewInvitationManager reviewInvitationManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager,  
			ReviewInvitationManager reviewInvitationManager, AccountManager accountManager, 
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
			statusChange.setType(Type.RESTORED_SOURCE_BRANCH);
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
			statusChange.setType(Type.DELETED_SOURCE_BRANCH);
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
	
	private void merge(PullRequest request) {
		MergePreview preview = Preconditions.checkNotNull(request.getMergePreview());
		String merged = Preconditions.checkNotNull(preview.getMerged());
		
		ObjectId mergedId = ObjectId.fromString(merged);
		RevCommit mergedCommit = request.getTargetDepot().getRevCommit(mergedId);
		
        PersonIdent committer = new PersonIdent(GitPlex.NAME, "");
        
		Depot targetDepot = request.getTargetDepot();
		MergeStrategy strategy = request.getMergeStrategy();
		if ((strategy == ALWAYS_MERGE || strategy == MERGE_IF_NECESSARY || strategy == SQUASH_MERGE) 
				&& !preview.getMerged().equals(preview.getRequestHead()) 
				&& !mergedCommit.getFullMessage().equals(request.getCommitMessage())) {
			try (	RevWalk revWalk = new RevWalk(targetDepot.getRepository());
					ObjectInserter inserter = targetDepot.getRepository().newObjectInserter()) {
		        CommitBuilder newCommit = new CommitBuilder();
		        newCommit.setAuthor(mergedCommit.getAuthorIdent());
		        newCommit.setCommitter(committer);
		        newCommit.setMessage(request.getCommitMessage());
		        newCommit.setTreeId(mergedCommit.getTree());
		        newCommit.setParentIds(mergedCommit.getParents());
		        mergedId = inserter.insert(newCommit);
		        merged = mergedId.name();
		        inserter.flush();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
	        preview = new MergePreview(preview.getTargetHead(), preview.getRequestHead(), 
	        		preview.getMergeStrategy(), merged);
	        request.setLastMergePreview(preview);
			RefUpdate refUpdate = targetDepot.updateRef(request.getMergeRef());
			refUpdate.setNewObjectId(mergedId);
			GitUtils.updateRef(refUpdate);
		}
		
		closeAsMerged(request, false, null);
		
		String targetRef = request.getTargetRef();
		ObjectId targetHeadId = ObjectId.fromString(preview.getTargetHead());
		RefUpdate refUpdate = targetDepot.updateRef(targetRef);
		refUpdate.setRefLogIdent(committer);
		refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
		refUpdate.setExpectedOldObjectId(targetHeadId);
		refUpdate.setNewObjectId(mergedId);
		GitUtils.updateRef(refUpdate);
		
		request.getTargetDepot().cacheObjectId(request.getTargetRef(), mergedId);
		
		Long requestId = request.getId();
		Subject subject = SecurityUtils.getSubject();
		ObjectId newTargetHeadId = mergedId;
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
		
		refUpdate = request.getTargetDepot().updateRef(request.getHeadRef());
		refUpdate.setNewObjectId(ObjectId.fromString(request.getHeadCommitHash()));
		GitUtils.updateRef(refUpdate);
		
		for (PullRequestUpdate update: request.getUpdates()) {
			pullRequestUpdateManager.save(update, false);
		}
		
		for (ReviewInvitation invitation: request.getReviewInvitations())
			reviewInvitationManager.save(invitation);

		checkAsync(request);
		
		listenerRegistry.post(new PullRequestOpened(request));
	}

	private void closeAsMerged(PullRequest request, boolean dueToMerged, String note) {
		Date date = new Date();
		
		if (dueToMerged)
			request.setLastMergePreview(null);
		
		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setCloseDate(date);
		closeInfo.setCloseStatus(CloseInfo.Status.MERGED);
		request.setCloseInfo(closeInfo);
		
		if (dueToMerged)
			note = "Source branch is already merged to target branch by some one";
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(date);
		statusChange.setNote(note);
		statusChange.setRequest(request);
		statusChange.setType(Type.MERGED);
		pullRequestStatusChangeManager.save(statusChange);
		request.setLastEvent(statusChange);
		
		save(request);
	}

	private void checkUpdate(PullRequest request) {
		if (!request.getHeadCommitHash().equals(request.getSource().getObjectName())) {
			ObjectId mergeBase = GitUtils.getMergeBase(
					request.getTargetDepot().getRepository(), request.getTarget().getObjectId(), 
					request.getSourceDepot().getRepository(), request.getSource().getObjectId(), 
					GitUtils.branch2ref(request.getSourceBranch()));
			if (mergeBase != null) {
				PullRequestUpdate update = new PullRequestUpdate();
				update.setRequest(request);
				update.setHeadCommitHash(request.getSource().getObjectName());
				update.setMergeBaseCommitHash(mergeBase.name());
				request.addUpdate(update);
				pullRequestUpdateManager.save(update, true);
				
				RefUpdate refUpdate = request.getTargetDepot().updateRef(request.getHeadRef());
				refUpdate.setNewObjectId(ObjectId.fromString(request.getHeadCommitHash()));
				GitUtils.updateRef(refUpdate);
			}
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
				checkUpdate(request);
				if (request.isMergeIntoTarget()) {
					closeAsMerged(request, true, null);
				} else {
					MergePreview mergePreview = request.getMergePreview();
					ReviewStatus checkStatus = request.getReviewStatus();					
					
					for (ReviewInvitation invitation: request.getReviewInvitations()) {
						if (checkStatus.getAwaitingReviewers().contains(invitation.getUser()))
							reviewInvitationManager.save(invitation);
					}
					
					boolean hasDisapprovals = false;
					for (PullRequestReview review: checkStatus.getEffectiveReviews().values()) {
						if (!review.isApproved()) {
							hasDisapprovals = true;
							break;
						}
					}
					if (!hasDisapprovals && checkStatus.getAwaitingReviewers().isEmpty() 
							&& mergePreview != null && mergePreview.getMerged() != null) {
						merge(request);
					}
				}
			}
		}
	}

	@Override
	public MergePreview previewMerge(PullRequest request) {
		if (request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE) {
			MergePreview lastPreview = request.getLastMergePreview();
			if (request.isOpen() && !request.isMergeIntoTarget() 
					&& (lastPreview == null || lastPreview.isObsolete(request))) {
				int priority = RequestCycle.get() != null?UI_PREVIEW_PRIORITY:BACKEND_PREVIEW_PRIORITY;			
				if (dao.getSession().getTransaction().getStatus() == TransactionStatus.ACTIVE) {
					doAfterCommit(new Runnable() {
	
						@Override
						public void run() {
							batchWorkManager.submit(getMergePreviewer(request), new Prioritized(priority));
						}
						
					});
				} else {
					batchWorkManager.submit(getMergePreviewer(request), new Prioritized(priority));
				}
				return null;
			} else {
				return lastPreview;
			}
		} else {
			return null;
		}
	}
	
	private BatchWorker getMergePreviewer(PullRequest request) {
		Long requestId = request.getId();
		return new BatchWorker("request-" + requestId + "-previewMerge", 1) {

			@Override
			public void doWork(Collection<Prioritized> works) {
				Preconditions.checkState(works.size() == 1);
				unitOfWork.call(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						PullRequest request = load(requestId);
						try {
							MergePreview preview = request.getLastMergePreview();
							if (request.isOpen() && !request.isMergeIntoTarget() && (preview == null || preview.isObsolete(request))) {
								logger.info("Calculating merge preview of pull request #{} in repository '{}'...", 
										request.getNumber(), request.getTargetDepot());
								String requestHead = request.getHeadCommitHash();
								String targetHead = request.getTarget().getObjectName();
								Depot targetDepot = request.getTargetDepot();
								preview = new MergePreview(targetHead, requestHead, request.getMergeStrategy(), null);
								request.setLastMergePreview(preview);
								String mergeRef = request.getMergeRef();
								ObjectId requestHeadId = ObjectId.fromString(requestHead);
								ObjectId targetHeadId = ObjectId.fromString(targetHead);
								if ((preview.getMergeStrategy() == MERGE_IF_NECESSARY) 
										&& GitUtils.isMergedInto(targetDepot.getRepository(), targetHeadId, requestHeadId)) {
									preview.setMerged(requestHead);
									RefUpdate refUpdate = targetDepot.updateRef(mergeRef);
									refUpdate.setNewObjectId(ObjectId.fromString(requestHead));
									GitUtils.updateRef(refUpdate);
								} else {
									PersonIdent user = new PersonIdent(GitPlex.NAME, "");
									ObjectId merged;
									if (preview.getMergeStrategy() == REBASE_MERGE) {
										merged = GitUtils.rebase(targetDepot.getRepository(), requestHeadId, targetHeadId, user);
									} else if (preview.getMergeStrategy() == SQUASH_MERGE) {
										merged = GitUtils.merge(targetDepot.getRepository(), 
												requestHeadId, targetHeadId, true, user, request.getCommitMessage());
									} else {
										merged = GitUtils.merge(targetDepot.getRepository(), 
												requestHeadId, targetHeadId, false, user, request.getCommitMessage());
									} 
									
									if (merged != null) {
										preview.setMerged(merged.name());
										RefUpdate refUpdate = targetDepot.updateRef(mergeRef);
										refUpdate.setNewObjectId(merged);
										GitUtils.updateRef(refUpdate);
									} else {
										RefUpdate refUpdate = targetDepot.updateRef(mergeRef);
										GitUtils.deleteRef(refUpdate);
									}
								}
								dao.persist(request);

								listenerRegistry.post(new MergePreviewCalculated(request));
								logger.info("Merge preview of pull request #{} in repository '{}' is calculated.", 
										request.getNumber(), request.getTargetDepot());						
							}
						} catch (Exception e) {
							logger.error("Error calculating pull request merge preview", e);
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
	public PullRequest findEffective(DepotAndBranch target, DepotAndBranch source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Criterion merged = Restrictions.and(
				Restrictions.eq("closeInfo.closeStatus", CloseInfo.Status.MERGED), 
				Restrictions.eq("lastMergePreview.requestHead", source.getObjectName()));
		
		criteria.add(ofTarget(target)).add(ofSource(source)).add(Restrictions.or(ofOpen(), merged));
		
		return find(criteria);
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
	public Collection<PullRequest> findAllOpenTo(DepotAndBranch target) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofTarget(target));
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
		if (type == Type.APPROVED || type == Type.DISAPPROVED || type == Type.WITHDRAWED_REVIEW 
				|| type == Type.REMOVED_REVIEWER || type == Type.ADDED_REVIEWER) {
			checkAsync(event.getRequest());
		}
	}
	
	@Listen
	public void on(MergePreviewCalculated event) {
		checkAsync(event.getRequest());
	}
	
	private Runnable newCheckStatusRunnable(Long requestId, Subject subject) {
		return new Runnable() {
			
			@Override
			public void run() {
				try {
			        ThreadContext.bind(subject);
					check(load(requestId));
				} catch (Exception e) {
					logger.error("Error checking pull request status", e);
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
			doUnitOfWorkAsyncAfterCommit(newCheckStatusRunnable(requestId, subject));
		} else {
			unitOfWork.doAsync(newCheckStatusRunnable(requestId, subject));
		}
	}

	@Transactional
	@Override
	public void saveMergeStrategy(PullRequest request) {
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setRequest(request);
		statusChange.setType(Type.CHANGED_MERGE_STRATEGY);
		statusChange.setUser(accountManager.getCurrent());
		
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		
		save(request);
	}

}
