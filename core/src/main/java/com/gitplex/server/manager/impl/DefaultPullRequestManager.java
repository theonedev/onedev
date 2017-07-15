package com.gitplex.server.manager.impl;

import static com.gitplex.server.model.PullRequest.CriterionHelper.ofOpen;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofSource;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofSourceProject;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofSubmitter;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofTarget;
import static com.gitplex.server.model.PullRequest.CriterionHelper.ofTargetProject;
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
import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.jgit.lib.Repository;
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
import com.gitplex.server.event.pullrequest.PullRequestMergePreviewCalculated;
import com.gitplex.server.event.pullrequest.PullRequestOpened;
import com.gitplex.server.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.event.pullrequest.PullRequestVerificationEvent;
import com.gitplex.server.event.pullrequest.PullRequestVerificationRunning;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.BatchWorkManager;
import com.gitplex.server.manager.MarkdownManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.manager.PullRequestUpdateManager;
import com.gitplex.server.manager.ReviewInvitationManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestStatusChange.Type;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.CloseInfo;
import com.gitplex.server.model.support.MergePreview;
import com.gitplex.server.model.support.MergeStrategy;
import com.gitplex.server.model.support.ProjectAndBranch;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.gitplex.server.persistence.dao.EntityRemoved;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.BatchWorker;
import com.gitplex.server.util.QualityCheckStatus;
import com.gitplex.server.util.QualityCheckStatusImpl;
import com.gitplex.server.util.Verification;
import com.gitplex.server.util.concurrent.Prioritized;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final UserManager userManager;
	
	private final UnitOfWork unitOfWork;
	
	private final ListenerRegistry listenerRegistry;
	
	private final ReviewInvitationManager reviewInvitationManager;
	
	private final BatchWorkManager batchWorkManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager,  
			ReviewInvitationManager reviewInvitationManager, UserManager userManager, 
			MarkdownManager markdownManager, BatchWorkManager batchWorkManager, 
			ListenerRegistry listenerRegistry, UnitOfWork unitOfWork, 
			PullRequestStatusChangeManager pullRequestStatusChangeManager) {
		super(dao);
		
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.reviewInvitationManager = reviewInvitationManager;
		this.userManager = userManager;
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
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null);
		if (request.getSource().getObjectName(false) == null) {
			RevCommit latestCommit = request.getHeadCommit();
			try {
				request.getSourceProject().git().branchCreate().setName(request.getSourceBranch()).setStartPoint(latestCommit).call();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
			request.getSourceProject().cacheObjectId(request.getSourceBranch(), latestCommit.copy());
			
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setNote(note);
			statusChange.setRequest(request);
			statusChange.setType(Type.RESTORED_SOURCE_BRANCH);
			statusChange.setUser(userManager.getCurrent());
			pullRequestStatusChangeManager.save(statusChange);
			
			request.setLastEvent(statusChange);
			save(request);
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			request.getSource().delete();
			
			PullRequestStatusChange statusChange = new PullRequestStatusChange();
			statusChange.setDate(new Date());
			statusChange.setNote(note);
			statusChange.setRequest(request);
			statusChange.setType(Type.DELETED_SOURCE_BRANCH);
			statusChange.setUser(userManager.getCurrent());
			pullRequestStatusChangeManager.save(statusChange);
			
			request.setLastEvent(statusChange);
			save(request);
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String note) {
		User user = userManager.getCurrent();
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
		User user = userManager.getCurrent();
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
		RevCommit mergedCommit = request.getTargetProject().getRevCommit(mergedId);
		
        PersonIdent committer = new PersonIdent(GitPlex.NAME, "");
        
		Project targetProject = request.getTargetProject();
		MergeStrategy strategy = request.getMergeStrategy();
		if ((strategy == ALWAYS_MERGE || strategy == MERGE_IF_NECESSARY || strategy == SQUASH_MERGE) 
				&& !preview.getMerged().equals(preview.getRequestHead()) 
				&& !mergedCommit.getFullMessage().equals(request.getCommitMessage(strategy))) {
			try (	RevWalk revWalk = new RevWalk(targetProject.getRepository());
					ObjectInserter inserter = targetProject.getRepository().newObjectInserter()) {
		        CommitBuilder newCommit = new CommitBuilder();
		        newCommit.setAuthor(mergedCommit.getAuthorIdent());
		        newCommit.setCommitter(committer);
		        newCommit.setMessage(request.getCommitMessage(strategy));
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
			RefUpdate refUpdate = GitUtils.getRefUpdate(targetProject.getRepository(), request.getMergeRef());
			refUpdate.setNewObjectId(mergedId);
			GitUtils.updateRef(refUpdate);
		}
		
		closeAsMerged(request, false, null);
		
		String targetRef = request.getTargetRef();
		ObjectId targetHeadId = ObjectId.fromString(preview.getTargetHead());
		RefUpdate refUpdate = GitUtils.getRefUpdate(targetProject.getRepository(), targetRef);
		refUpdate.setRefLogIdent(committer);
		refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
		refUpdate.setExpectedOldObjectId(targetHeadId);
		refUpdate.setNewObjectId(mergedId);
		GitUtils.updateRef(refUpdate);
		
		request.getTargetProject().cacheObjectId(request.getTargetRef(), mergedId);
		
		Long requestId = request.getId();
		Subject subject = SecurityUtils.getSubject();
		ObjectId newTargetHeadId = mergedId;
		doUnitOfWorkAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				try {
					PullRequest request = load(requestId);
					request.getTargetProject().cacheObjectId(request.getTargetRef(), newTargetHeadId);
					listenerRegistry.post(new RefUpdated(request.getTargetProject(), targetRef, 
								targetHeadId, newTargetHeadId));
				} finally {
					ThreadContext.unbindSubject();
				}
			}
			
		});
	}
	
	private long getNextNumber(Project project) {
		AtomicLong nextNumber;
		synchronized (nextNumbers) {
			nextNumber = nextNumbers.get(project.getUUID());
		}
		if (nextNumber == null) {
			long maxNumber;
			Query query = getSession().createQuery("select max(number) from PullRequest where targetProject=:project");
			query.setParameter("project", project);
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
				nextNumber = nextNumbers.get(project.getUUID());
				if (nextNumber == null) {
					nextNumber = new AtomicLong(maxNumber+1);
					nextNumbers.put(project.getUUID(), nextNumber);
				}
			}
		} 
		return nextNumber.getAndIncrement();
	}
	
	@Transactional
	@Override
	public void open(PullRequest request) {
		request.setNumber(getNextNumber(request.getTargetProject()));
		dao.persist(request);
		
		RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), request.getBaseRef());
		refUpdate.setNewObjectId(ObjectId.fromString(request.getBaseCommitHash()));
		GitUtils.updateRef(refUpdate);
		
		refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), request.getHeadRef());
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
					request.getTargetProject().getRepository(), request.getTarget().getObjectId(), 
					request.getSourceProject().getRepository(), request.getSource().getObjectId(), 
					GitUtils.branch2ref(request.getSourceBranch()));
			if (mergeBase != null) {
				PullRequestUpdate update = new PullRequestUpdate();
				update.setRequest(request);
				update.setHeadCommitHash(request.getSource().getObjectName());
				update.setMergeBaseCommitHash(mergeBase.name());
				request.addUpdate(update);
				pullRequestUpdateManager.save(update, true);
				
				RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), 
						request.getHeadRef());
				refUpdate.setNewObjectId(ObjectId.fromString(request.getHeadCommitHash()));
				GitUtils.updateRef(refUpdate);
			}
		}
	}

	@Transactional
	@Override
	public void check(PullRequest request) {
		if (request.isOpen()) {
			if (request.getSourceProject() == null) {
				discard(request, "Source project no longer exists");
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
					QualityCheckStatus qualityCheckStatus = request.getQualityCheckStatus();					
					
					for (ReviewInvitation invitation: request.getReviewInvitations()) {
						if (qualityCheckStatus.getAwaitingReviewers().contains(invitation.getUser()))
							reviewInvitationManager.save(invitation);
					}
					
					boolean hasDisapprovedReviews = false;
					for (Review review: qualityCheckStatus.getEffectiveReviews().values()) {
						if (!review.isApproved()) {
							hasDisapprovedReviews = true;
							break;
						}
					}
					boolean hasUnsuccessVerifications = false;
					for (Verification verification: qualityCheckStatus.getEffectiveVerifications().values()) {
						if (verification.getStatus() != Verification.Status.SUCCESS) {
							hasUnsuccessVerifications = true;
							break;
						}
					}
					
					if (!hasDisapprovedReviews && !hasUnsuccessVerifications 
							&& qualityCheckStatus.getAwaitingReviewers().isEmpty()
							&& qualityCheckStatus.getAwaitingVerifications().isEmpty()
							&& mergePreview != null && mergePreview.getMerged() != null) {
						merge(request);
					}
				}
			}
		}
	}

	@Sessional
	@Override
	public MergePreview previewMerge(PullRequest request) {
		if (!request.isNew() && request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE) {
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
			public void doWorks(Collection<Prioritized> works) {
				try {
					Preconditions.checkState(works.size() == 1);
					
					AtomicReference<String> projectNameRef = new AtomicReference<>(null);
					AtomicReference<Long> requestNumberRef = new AtomicReference<>(null);
					AtomicReference<Repository> repositoryRef = new AtomicReference<>(null);
					AtomicReference<String> mergeRefRef = new AtomicReference<>(null);
					
					MergePreview mergePreview = unitOfWork.call(new Callable<MergePreview>() {

						@Override
						public MergePreview call() throws Exception {
							PullRequest request = load(requestId);
							Project targetProject = request.getTargetProject();
							requestNumberRef.set(request.getNumber());
							projectNameRef.set(targetProject.getName());	
							repositoryRef.set(targetProject.getRepository());
							mergeRefRef.set(request.getMergeRef());
							
							MergePreview mergePreview = request.getLastMergePreview();
							if (request.isOpen() 
									&& !request.isMergeIntoTarget() 
									&& (mergePreview == null || mergePreview.isObsolete(request))) {
								return new MergePreview(request.getTarget().getObjectName(), 
										request.getHeadCommitHash(), request.getMergeStrategy(), null);
							} else {
								return null;
							}
						}
					});
					
					if (mergePreview != null) {
						logger.debug("Calculating merge preview of pull request #{} in project '{}'...", 
								requestNumberRef.get(), projectNameRef.get());
						ObjectId targetHeadId = ObjectId.fromString(mergePreview.getTargetHead());
						ObjectId requestHeadId = ObjectId.fromString(mergePreview.getRequestHead());
						
						if ((mergePreview.getMergeStrategy() == MERGE_IF_NECESSARY) 
								&& GitUtils.isMergedInto(repositoryRef.get(), targetHeadId, requestHeadId)) {
							mergePreview.setMerged(mergePreview.getRequestHead());
							RefUpdate refUpdate = GitUtils.getRefUpdate(repositoryRef.get(), mergeRefRef.get());
							refUpdate.setNewObjectId(requestHeadId);
							GitUtils.updateRef(refUpdate);
						} else {
							PersonIdent user = new PersonIdent(GitPlex.NAME, "");
							ObjectId merged;
							if (mergePreview.getMergeStrategy() == REBASE_MERGE) {
								merged = GitUtils.rebase(repositoryRef.get(), requestHeadId, targetHeadId, user);
							} else if (mergePreview.getMergeStrategy() == SQUASH_MERGE) {
								merged = GitUtils.merge(repositoryRef.get(), requestHeadId, targetHeadId, true, user, 
										request.getCommitMessage(mergePreview.getMergeStrategy()));
							} else {
								merged = GitUtils.merge(repositoryRef.get(), requestHeadId, targetHeadId, false, user, 
										request.getCommitMessage(mergePreview.getMergeStrategy()));
							} 
							
							RefUpdate refUpdate = GitUtils.getRefUpdate(repositoryRef.get(), mergeRefRef.get());
							if (merged != null) {
								mergePreview.setMerged(merged.name());
								refUpdate.setNewObjectId(merged);
								GitUtils.updateRef(refUpdate);
							} else {
								GitUtils.deleteRef(refUpdate);
							}
						}
						
						unitOfWork.call(new Callable<Void>() {
							
							@Override
							public Void call() throws Exception {
								PullRequest request = load(requestId);
								request.setLastMergePreview(mergePreview);
								dao.persist(request);
								listenerRegistry.post(new PullRequestMergePreviewCalculated(request));
								return null;
							}
						
						});
					}
				} catch (Exception e) {
					logger.error("Error calculating pull request merge preview", e);
				}
			}
			
		};
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	for (PullRequest request: project.getOutgoingRequests()) {
	    		if (!request.getTargetProject().equals(project) && request.isOpen())
	        		discard(request, "Source project is deleted.");
	    	}
	    	
	    	Query query = getSession().createQuery("update PullRequest set sourceProject=null where "
	    			+ "sourceProject=:project");
	    	query.setParameter("project", project);
	    	query.executeUpdate();
		}
	}
	
	@Transactional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		if (branch != null) {
			ProjectAndBranch projectAndBranch = new ProjectAndBranch(event.getProject(), branch);
			Criterion criterion = Restrictions.and(
					ofOpen(), 
					Restrictions.or(ofSource(projectAndBranch), ofTarget(projectAndBranch)));
			for (PullRequest request: findAll(EntityCriteria.of(PullRequest.class).add(criterion))) {
				check(request);
			}
		}
	}

	@Sessional
	@Override
	public PullRequest findEffective(ProjectAndBranch target, ProjectAndBranch source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Criterion merged = Restrictions.and(
				Restrictions.eq("closeInfo.closeStatus", CloseInfo.Status.MERGED), 
				Restrictions.eq("lastMergePreview.requestHead", source.getObjectName()));
		
		criteria.add(ofTarget(target)).add(ofSource(source)).add(Restrictions.or(ofOpen(), merged));
		
		return find(criteria);
	}
	
	@Sessional
	@Override
	public PullRequest findLatest(Project project, User submitter) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSourceProject(project), ofTargetProject(project)));
		criteria.add(ofSubmitter(submitter));
		criteria.addOrder(Order.desc("id"));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Collection<PullRequest> findAllOpenTo(ProjectAndBranch target) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofTarget(target));
		criteria.add(ofOpen());
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<PullRequest> findAllOpen(ProjectAndBranch sourceOrTarget) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSource(sourceOrTarget), ofTarget(sourceOrTarget)));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public int countOpen(Project project) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(PullRequest.CriterionHelper.ofOpen());
		criteria.add(PullRequest.CriterionHelper.ofTargetProject(project));
		return count(criteria);
	}

	@Sessional
	@Override
	public PullRequest find(Project target, long number) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("targetProject", target));
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
	public void on(PullRequestVerificationEvent event) {
		if (!(event instanceof PullRequestVerificationRunning))
			checkAsync(event.getRequest());
	}
	
	@Listen
	public void on(PullRequestMergePreviewCalculated event) {
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
		statusChange.setUser(userManager.getCurrent());
		
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		
		save(request);
	}

	@Transactional
	@Override
	public QualityCheckStatus checkQuality(PullRequest request) {
		return new QualityCheckStatusImpl(request);
	}

	@Transactional
	@Override
	public Collection<PullRequest> findOpenByVerifyCommit(String commitHash) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(PullRequest.CriterionHelper.ofOpen());
		Criterion verifyCommitCriterion = Restrictions.or(
				Restrictions.eq("headCommitHash", commitHash), 
				Restrictions.eq("lastMergePreview.merged", commitHash));
		criteria.add(verifyCommitCriterion);
		return findAll(criteria);
	}

}
