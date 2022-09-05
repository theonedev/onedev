package io.onedev.server.entitymanager.impl;

import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Objects;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.Listen;
import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.PendingSuggestionApplyManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entityreference.EntityReferenceManager;
import io.onedev.server.entityreference.ReferencedFromAware;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.pullrequest.PullRequestAssigned;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestChanged;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.event.pullrequest.PullRequestReviewRequested;
import io.onedev.server.event.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.event.pullrequest.PullRequestUnassigned;
import io.onedev.server.event.pullrequest.PullRequestUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.FileProtection;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeStrategyChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestSourceBranchDeleteData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestSourceBranchRestoreData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestTargetBranchChangeData;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStats;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;

@Singleton
public class DefaultPullRequestManager extends BaseEntityManager<PullRequest> implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int PREVIEW_CALC_PRIORITY = 50;
	
	private final PullRequestUpdateManager updateManager;
	
	private final ProjectManager projectManager;
	
	private final SessionManager sessionManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestReviewManager reviewManager;
	
	private final BuildManager buildManager;
	
	private final CommitInfoManager commitInfoManager;

	private final BatchWorkManager batchWorkManager;
	
	private final PullRequestChangeManager changeManager;
	
	private final TransactionManager transactionManager;
	
	private final ExecutorService executorService;
	
	private final EntityReferenceManager referenceManager;
	
	private final PendingSuggestionApplyManager pendingSuggestionApplyManager;
	
	private final SettingManager settingManager;
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager updateManager,  
			PullRequestReviewManager reviewManager, MarkdownManager markdownManager, 
			BatchWorkManager batchWorkManager, ListenerRegistry listenerRegistry, 
			SessionManager sessionManager, PullRequestChangeManager changeManager, 
			ExecutorService executorService, BuildManager buildManager, 
			TransactionManager transactionManager, ProjectManager projectManager, 
			CommitInfoManager commitInfoManager, EntityReferenceManager referenceManager, 
			PendingSuggestionApplyManager pendingSuggestionApplyManager, 
			SettingManager settingManager) {
		super(dao);
		
		this.updateManager = updateManager;
		this.reviewManager = reviewManager;
		this.transactionManager = transactionManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.listenerRegistry = listenerRegistry;
		this.changeManager = changeManager;
		this.buildManager = buildManager;
		this.executorService = executorService;
		this.projectManager = projectManager;
		this.commitInfoManager = commitInfoManager;
		this.referenceManager = referenceManager;
		this.pendingSuggestionApplyManager = pendingSuggestionApplyManager;
		this.settingManager = settingManager;
	}
	
	@Transactional
	@Override
	public void delete(PullRequest request) {
		request.deleteRefs();
		for (PullRequestUpdate update: request.getUpdates())
			update.deleteRefs();
		
    	Query<?> query = getSession().createQuery(String.format(
    			"update CodeComment set %s=null where %s=:request", 
    			CodeComment.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST, 
    			CodeComment.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST));
    	query.setParameter("request", request);
    	query.executeUpdate();

    	query = getSession().createQuery(String.format("update CodeCommentReply set %s=null where %s=:request",
    			CodeCommentReply.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST,
    			CodeCommentReply.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST));
    	query.setParameter("request", request);
    	query.executeUpdate();
    	
    	query = getSession().createQuery(String.format("update CodeCommentStatusChange set %s=null where %s=:request",
    			CodeCommentStatusChange.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST,
    			CodeCommentStatusChange.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST));
    	query.setParameter("request", request);
    	query.executeUpdate();
    	
		dao.remove(request);
	}

	@Transactional
	@Override
	public void restoreSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null);
		if (request.getSource().getObjectName(false) == null) {
			RevCommit latestCommit = request.getLatestUpdate().getHeadCommit();
			try {
				request.getSourceProject().git().branchCreate().setName(request.getSourceBranch()).setStartPoint(latestCommit).call();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			request.getSourceProject().cacheObjectId(request.getSourceBranch(), latestCommit.copy());
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setData(new PullRequestSourceBranchRestoreData());
			change.setRequest(request);
			change.setUser(SecurityUtils.getUser());
			changeManager.save(change, note);
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			projectManager.deleteBranch(request.getSourceProject(), request.getSourceBranch());
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setData(new PullRequestSourceBranchDeleteData());
			change.setRequest(request);
			change.setUser(SecurityUtils.getUser());
			changeManager.save(change, note);
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String note) {
		User user = SecurityUtils.getUser();
		request.setStatus(Status.OPEN);

		PullRequestChange change = new PullRequestChange();
		change.setData(new PullRequestReopenData());
		change.setRequest(request);
		change.setUser(user);
		changeManager.save(change, note);
		
		MergePreview mergePreview = request.getMergePreview();
		if (mergePreview != null && mergePreview.getMergeCommitHash() != null)
			listenerRegistry.post(new PullRequestMergePreviewCalculated(request));
		
		checkAsync(Lists.newArrayList(request), null);
	}

	@Transactional
	@Override
 	public void discard(PullRequest request, String note) {
		request.setStatus(Status.DISCARDED);
		
		User user = SecurityUtils.getUser();
		
		PullRequestChange change = new PullRequestChange();
		change.setData(new PullRequestDiscardData());
		change.setRequest(request);
		change.setUser(user);
		changeManager.save(change, note);
		
		pendingSuggestionApplyManager.discard(null, request);
	}
	
	@Transactional
	@Override
	public void merge(PullRequest request, String commitMessage) {
		MergePreview mergePreview = Preconditions.checkNotNull(request.getMergePreview());
		ObjectId mergeCommitId = ObjectId.fromString(
				Preconditions.checkNotNull(mergePreview.getMergeCommitHash()));
		User user = SecurityUtils.getUser();
        PersonIdent person = user.asPerson();
        
		Project project = request.getTargetProject();
		MergeStrategy mergeStrategy = mergePreview.getMergeStrategy();
		
		PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
		
		if (mergeStrategy == CREATE_MERGE_COMMIT
				|| mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS
				|| mergeStrategy == CREATE_MERGE_COMMIT_IF_NECESSARY && !mergeCommitId.name().equals(mergePreview.getHeadCommitHash())) {
			try (	RevWalk revWalk = new RevWalk(project.getRepository());
					ObjectInserter inserter = project.getRepository().newObjectInserter()) {
				RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
		        CommitBuilder commitBuilder = new CommitBuilder();
		        if (mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS)
		        	commitBuilder.setAuthor(mergeCommit.getAuthorIdent());
		        else
		        	commitBuilder.setAuthor(person);
		        commitBuilder.setCommitter(person);
		        commitBuilder.setMessage(commitMessage);
		        commitBuilder.setTreeId(mergeCommit.getTree());
		        commitBuilder.setParentIds(mergeCommit.getParents());
		        if (signingKey != null)
		        	GitUtils.sign(commitBuilder, signingKey);
		        mergeCommitId = inserter.insert(commitBuilder);
		        inserter.flush();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		} else if (mergeStrategy == REBASE_SOURCE_BRANCH_COMMITS) {
			try (	RevWalk revWalk = new RevWalk(project.getRepository());
					ObjectInserter inserter = project.getRepository().newObjectInserter()) {
				RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
				ObjectId targetHeadCommitId = ObjectId.fromString(mergePreview.getTargetHeadCommitHash());
				RevCommit targetHeadCommit = revWalk.parseCommit(targetHeadCommitId);
	    		List<RevCommit> commits = RevWalkUtils.find(revWalk, mergeCommit, targetHeadCommit);
	    		Collections.reverse(commits);
	    		mergeCommit = targetHeadCommit;
	    		for (RevCommit commit: commits) {
	    			PersonIdent committer = commit.getCommitterIdent();
	    			if (committer.getName().equals(User.ONEDEV_NAME) || !commit.getParent(0).equals(mergeCommit)) {
				        CommitBuilder commitBuilder = new CommitBuilder();
				        commitBuilder.setAuthor(commit.getAuthorIdent());
				        commitBuilder.setCommitter(person);
				        commitBuilder.setParentId(mergeCommit.copy());
				        commitBuilder.setMessage(commit.getFullMessage());
				        commitBuilder.setTreeId(commit.getTree().getId());
				        if (signingKey != null)
				        	GitUtils.sign(commitBuilder, signingKey);
				        mergeCommit = revWalk.parseCommit(inserter.insert(commitBuilder));
	    			} else {
	    				mergeCommit = commit;
	    			}
	    		}
	    		mergeCommitId = mergeCommit.copy();
		        inserter.flush();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		
		if (!mergeCommitId.name().equals(mergePreview.getMergeCommitHash())) {
	        mergePreview = new MergePreview(mergePreview.getTargetHeadCommitHash(), 
	        		mergePreview.getHeadCommitHash(), mergeStrategy, mergeCommitId.name());
	        request.setLastMergePreview(mergePreview);
	        request.writeMergeRef();
		}
		
		closeAsMerged(request, false);
		
		String targetRef = request.getTargetRef();
		ObjectId targetHeadCommitId = ObjectId.fromString(mergePreview.getTargetHeadCommitHash());
		RefUpdate refUpdate = GitUtils.getRefUpdate(project.getRepository(), targetRef);
		refUpdate.setRefLogIdent(person);
		refUpdate.setRefLogMessage("Pull request #" + request.getNumber(), true);
		refUpdate.setExpectedOldObjectId(targetHeadCommitId);
		refUpdate.setNewObjectId(mergeCommitId);
		GitUtils.updateRef(refUpdate);
		
		request.getTargetProject().cacheObjectId(request.getTargetRef(), mergeCommitId);
		
		Long requestId = request.getId();
		ObjectId newTargetHeadId = mergeCommitId;
		sessionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				PullRequest request = load(requestId);
				request.getTargetProject().cacheObjectId(request.getTargetRef(), newTargetHeadId);
				listenerRegistry.post(new RefUpdated(request.getTargetProject(), targetRef, 
							targetHeadCommitId, newTargetHeadId));
			}
			
		});
	}
	
	@Transactional
	@Override
	public void open(PullRequest request) {
		Preconditions.checkArgument(request.isNew());
		
		request.setNumberScope(request.getTargetProject().getForkRoot());
		request.setNumber(getNextNumber(request.getNumberScope()));
		
		PullRequestOpened event = new PullRequestOpened(request);
		request.setLastUpdate(event.getLastUpdate());
		
		dao.persist(request);
		request.writeBaseRef();
		request.writeHeadRef();
		
		for (PullRequestUpdate update: request.getUpdates())
			updateManager.save(update);

		for (PullRequestReview review: request.getReviews())
			dao.persist(review);
		
		for (PullRequestAssignment assignment: request.getAssignments())
			dao.persist(assignment);

		MergePreview mergePreview = request.getMergePreview();
		if (mergePreview != null && mergePreview.getMergeCommitHash() != null)
			listenerRegistry.post(new PullRequestMergePreviewCalculated(request));
		
		checkAsync(Lists.newArrayList(request), null);
		
		listenerRegistry.post(event);
	}
	
	private void closeAsMerged(PullRequest request, boolean dueToMerged) {
		request.setStatus(Status.MERGED);
		
		Date date = new DateTime().plusMillis(1).toDate();
		
		if (dueToMerged)
			request.setLastMergePreview(null);
		
		String reason;
		if (dueToMerged)
			reason = "merged pull request";
		else
			reason = null;
		
		PullRequestChange change = new PullRequestChange();
		change.setUser(SecurityUtils.getUser());
		change.setDate(date);
		change.setData(new PullRequestMergeData(reason));
		change.setRequest(request);
		changeManager.save(change);
		
		pendingSuggestionApplyManager.discard(null, request);
	}

	@Transactional
	@Override
	public void check(PullRequest request, boolean sourceUpdated) {
		try {
			request.setCheckError(null);
			if (request.isOpen() && request.isValid()) {
				if (request.getSourceProject() == null) {
					discard(request, "Source project no longer exists");
				} else if (request.getSource().getObjectId(false) == null) {
					discard(request, "Source branch no longer exists");
				} else if (request.getTarget().getObjectId(false) == null) {
					discard(request, "Target branch no longer exists");
				} else {
					updateManager.checkUpdate(request);
					if (request.isMergedIntoTarget()) {
						closeAsMerged(request, true);
					} else {
						checkReviews(request, sourceUpdated);
						
						/*
						 * If the check method runs concurrently, below statements may fail. It will 
						 * not do any harm except that the transaction rolls back
						 */
						for (PullRequestReview review: request.getReviews()) {
							if (review.isNew() || review.isDirty())
								reviewManager.save(review);
						}
						
						Long requestId = request.getId();
						transactionManager.runAfterCommit(new Runnable() {
							
							@Override
							public void run() {
								BatchWorker previewCalcWorker = new BatchWorker("request-" + requestId + "-previewMerge", 1) {

									@Override
									public void doWorks(Collection<Prioritized> works) {
										sessionManager.run(new Runnable() {

											@Override
											public void run() {
												Preconditions.checkState(works.size() == 1);
												PullRequest request = load(requestId);
												Project targetProject = request.getTargetProject();
												if (request.isOpen() && !request.isMergedIntoTarget()) {
													MergePreview mergePreview = request.getMergePreview();
													if (mergePreview == null) {
														mergePreview = new MergePreview(request.getTarget().getObjectName(), 
																request.getLatestUpdate().getHeadCommitHash(), request.getMergeStrategy(), null);
														logger.debug("Calculating merge preview of pull request #{} in project '{}'...", 
																request.getNumber(), targetProject.getPath());
														ObjectId merged = mergePreview.getMergeStrategy().merge(request, "Merge preview of pull request #" + request.getNumber());
														if (merged != null)
															mergePreview.setMergeCommitHash(merged.name());
														mergePreview.syncRef(request);
														request.setLastMergePreview(mergePreview);
														dao.persist(request);
														listenerRegistry.post(new PullRequestMergePreviewCalculated(request));
													} else {
														mergePreview.syncRef(request);
													}
												} 
											}
											
										});
									}
									
								};						
								batchWorkManager.submit(previewCalcWorker, new Prioritized(PREVIEW_CALC_PRIORITY));								
							}
							
						});
					}
				}
			}
		} catch (Exception e) {
			if (e.getMessage() != null)
				request.setCheckError("Error checking pull request: " + e.getMessage());
			else
				request.setCheckError("Error checking pull request");
			request.setCheckError(request.getCheckError() + ", check server log for details");
				
			String message = String.format("Error checking pull request (project: %s, number: #%d)", 
					request.getTargetProject().getPath(), request.getNumber());
			logger.error(message, e);
		}
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
	    	
	    	Query<?> query = getSession().createQuery("update PullRequest set sourceProject=null where "
	    			+ "sourceProject=:project");
	    	query.setParameter("project", project);
	    	query.executeUpdate();
		}
	}
	
	@Transactional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		if (branch != null && !event.getOldCommitId().equals(ObjectId.zeroId())) {
			ProjectAndBranch projectAndBranch = new ProjectAndBranch(event.getProject(), branch);
			Criterion criterion = Restrictions.and(
					ofOpen(), 
					Restrictions.or(ofSource(projectAndBranch), ofTarget(projectAndBranch)));
			checkAsync(query(EntityCriteria.of(PullRequest.class).add(criterion)), projectAndBranch);
		}
	}

	@Sessional
	@Override
	public PullRequest findEffective(ProjectAndBranch target, ProjectAndBranch source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Criterion merged = Restrictions.and(
				Restrictions.eq(PullRequest.PROP_STATUS, Status.MERGED), 
				Restrictions.eq(PullRequest.PROP_LAST_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH, source.getObjectName()));
		
		criteria.add(ofTarget(target)).add(ofSource(source)).add(Restrictions.or(ofOpen(), merged));
		
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Map<ProjectAndBranch, PullRequest> findEffectives(ProjectAndBranch target, Collection<ProjectAndBranch> sources) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Collection<Criterion> criterions = new ArrayList<>();
		for (ProjectAndBranch source: sources) {
			Criterion merged = Restrictions.and(
					Restrictions.eq(PullRequest.PROP_STATUS, Status.MERGED), 
					Restrictions.eq(PullRequest.PROP_LAST_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH, source.getObjectName()));
			criterions.add(Restrictions.and(ofTarget(target), ofSource(source), Restrictions.or(ofOpen(), merged)));
		}
		criteria.add(Restrictions.or(criterions.toArray(new Criterion[0])));
		
		Map<ProjectAndBranch, PullRequest> requests = new HashMap<>();
		for(PullRequest request: query(criteria)) 
			requests.put(new ProjectAndBranch(request.getSourceProject(), request.getSourceBranch()), request);
		
		return requests;
	}
	
	@Sessional
	@Override
	public PullRequest findLatest(Project project) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSourceProject(project), ofTargetProject(project)));
		criteria.addOrder(Order.desc(PullRequest.PROP_ID));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Collection<PullRequest> queryOpenTo(ProjectAndBranch target) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofTarget(target));
		criteria.add(ofOpen());
		return query(criteria);
	}

	@Sessional
	@Override
	public Collection<PullRequest> queryOpen(ProjectAndBranch sourceOrTarget) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSource(sourceOrTarget), ofTarget(sourceOrTarget)));
		return query(criteria);
	}

	@Transactional
	@Listen
	public void on(PullRequestChanged event) {
		PullRequestChangeData data = event.getChange().getData();
		if (data instanceof PullRequestApproveData || data instanceof PullRequestDiscardData  
				|| data instanceof PullRequestMergeStrategyChangeData
				|| data instanceof PullRequestTargetBranchChangeData) {
			checkAsync(Lists.newArrayList(event.getRequest()), null);
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (event instanceof PullRequestUpdated) {
			Collection<User> committers = ((PullRequestUpdated) event).getCommitters();
			if (committers.size() == 1) {
				LastUpdate lastUpdate = new LastUpdate();
				lastUpdate.setUser(committers.iterator().next());
				lastUpdate.setActivity("added commits");
				lastUpdate.setDate(event.getDate());
				event.getRequest().setLastUpdate(lastUpdate);
			} else {
				LastUpdate lastUpdate = new LastUpdate();
				lastUpdate.setActivity("Commits added");
				lastUpdate.setDate(event.getDate());
				event.getRequest().setLastUpdate(lastUpdate);
			}
		} else {
			boolean minorChange = false;
			if (event instanceof PullRequestChanged) {
				PullRequestChangeData changeData = ((PullRequestChanged)event).getChange().getData();
				if (changeData instanceof PullRequestSourceBranchDeleteData
						|| changeData instanceof PullRequestSourceBranchRestoreData
						|| changeData instanceof ReferencedFromAware) {
					minorChange = true;
				}
			}
			if (!(event instanceof PullRequestOpened 
					|| event instanceof PullRequestMergePreviewCalculated
					|| event instanceof PullRequestBuildEvent
					|| event instanceof PullRequestAssigned
					|| event instanceof PullRequestUnassigned
					|| event instanceof PullRequestReviewRequested
					|| event instanceof PullRequestReviewerRemoved
					|| minorChange)) {
				event.getRequest().setLastUpdate(event.getLastUpdate());
				if (event instanceof PullRequestCodeCommentEvent)
					event.getRequest().setCodeCommentsUpdateDate(event.getDate());
			}
		}
	}
	
	@Listen
	@Transactional
	public void on(BuildEvent event) {
		Build build = event.getBuild();
		if (build.getRequest() != null) {
			if (build.getRequest().isDiscarded()) {
				if (build.getRequest().getLatestUpdate().getTargetHeadCommitHash().equals(build.getCommitHash()))
					listenerRegistry.post(new PullRequestBuildEvent(build));
			} else {
				MergePreview mergePreview = build.getRequest().getMergePreview();
				if (mergePreview != null && build.getCommitHash().equals(mergePreview.getMergeCommitHash()))
					listenerRegistry.post(new PullRequestBuildEvent(build));
			}
		}
	}
	
	@Sessional
	protected void checkAsync(Collection<PullRequest> requests, @Nullable ProjectAndBranch source) {
		Collection<Long> requestIds = requests.stream().map(it->it.getId()).collect(Collectors.toList());
		if (!requestIds.isEmpty()) {
			transactionManager.runAfterCommit(new Runnable() {
	
				@Override
				public void run() {
					executorService.execute(new Runnable() {

						@Override
						public void run() {
					        for (Long requestId: requestIds) {
					        	/* 
					        	 * Lock here to minimize concurrent checks against the same pull request. We 
					        	 * can not lock the check method directly as the lock should be put outside 
					        	 * of transaction
					        	 */
					        	LockUtils.call(PullRequest.getSerialLockName(requestId), true, new Callable<Void>() {

									@Override
									public Void call() throws Exception {
										sessionManager.run(new Runnable() {

											@Override
											public void run() {
												PullRequest request = load(requestId);
									        	check(request, request.getSource() != null && request.getSource().equals(source));
											}
											
										});
										return null;
									}
					        		
					        	});
					        }
						}
						
					});
				}
				
			});
		}
	}
	
	@Transactional
	@Override
	public void checkReviews(PullRequest request, boolean sourceUpdated) {
		BranchProtection branchProtection = request.getTargetProject()
				.getHierarchyBranchProtection(request.getTargetBranch(), request.getSubmitter());
		Collection<String> changedFiles;
		if (sourceUpdated) {
			changedFiles = request.getLatestUpdate().getChangedFiles();
		} else {
			changedFiles = new HashSet<>();
			for (PullRequestUpdate update: request.getUpdates())
				changedFiles.addAll(update.getChangedFiles());
		}
		checkReviews(branchProtection.getParsedReviewRequirement(), 
				request, changedFiles, sourceUpdated);
		
		ReviewRequirement checkedRequirement = ReviewRequirement.parse(null, true);
		
		for (String file: changedFiles) {
			FileProtection fileProtection = branchProtection.getFileProtection(file);
			if (!checkedRequirement.covers(fileProtection.getParsedReviewRequirement())) {
				checkedRequirement.mergeWith(fileProtection.getParsedReviewRequirement());
				checkReviews(fileProtection.getParsedReviewRequirement(), 
						request, Sets.newHashSet(file), sourceUpdated);
			}
		}
	}

	private void checkReviews(ReviewRequirement reviewRequirement, PullRequest request, 
			Collection<String> changedFiles, boolean sourceUpdated) {
		for (User user: reviewRequirement.getUsers()) {
			if (!user.equals(request.getSubmitter())) {
				PullRequestReview review = request.getReview(user);
				if (review == null) {
					review = new PullRequestReview();
					review.setRequest(request);
					review.setUser(user);
					request.getReviews().add(review);
				} else if (review.getStatus() == PullRequestReview.Status.EXCLUDED 
						|| sourceUpdated && review.getStatus() != PullRequestReview.Status.PENDING) {
					review.setStatus(PullRequestReview.Status.PENDING);
				}
			}
		}
		
		for (Map.Entry<Group, Integer> entry: reviewRequirement.getGroups().entrySet()) {
			Group group = entry.getKey();
			int requiredCount = entry.getValue();
			Collection<User> users = new ArrayList<>(group.getMembers());
			users.remove(request.getSubmitter());
			checkReviews(users, requiredCount, request, changedFiles, sourceUpdated);
		}
	}
	
	private void checkReviews(Collection<User> users, int requiredCount, 
			PullRequest request, Collection<String> changedFiles, boolean sourceUpdated) {
		if (requiredCount == 0)
			requiredCount = users.size();
		for (User user: users) {
			PullRequestReview review = request.getReview(user);
			if (review != null && review.getStatus() != PullRequestReview.Status.EXCLUDED) {
				if (sourceUpdated && review.getStatus() != PullRequestReview.Status.PENDING)
					review.setStatus(PullRequestReview.Status.PENDING);
				requiredCount--;
			}
			if (requiredCount <= 0)
				break;
		}

		Project project = request.getTargetProject();
		
		Set<User> reviewers = new HashSet<>();
		
		if (requiredCount > 0) {
			List<User> candidateReviewers = new ArrayList<>();
			
			for (User user: users) {
				if (request.getReview(user) == null)
					candidateReviewers.add(user);
			}
			
			commitInfoManager.sortUsersByContribution(candidateReviewers, project, changedFiles);
			
			for (User user: candidateReviewers) {
				reviewers.add(user);
				PullRequestReview review = new PullRequestReview();
				review.setRequest(request);
				review.setUser(user);
				request.getReviews().add(review);
				if (reviewers.size() == requiredCount)
					break;
			}
		}
		
		if (requiredCount > reviewers.size()) {
			List<PullRequestReview> reviews = new ArrayList<>(request.getReviews());
			Collections.sort(reviews, new Comparator<PullRequestReview>() {

				@Override
				public int compare(PullRequestReview o1, PullRequestReview o2) {
					return o1.getStatusDate().compareTo(o2.getStatusDate());
				}
				
			});
			for (PullRequestReview review: reviews) {
				if (review.getStatus() == PullRequestReview.Status.EXCLUDED) { 
					reviewers.add(review.getUser());
					review.setStatus(PullRequestReview.Status.PENDING);
					if (reviewers.size() == requiredCount)
						break;
				}
			}
		}
		if (requiredCount > reviewers.size()) {
			String errorMessage = String.format("Impossible to provide required number of reviewers "
					+ "(candidates: %s, required number of reviewers: %d, pull request: #%d)", 
					reviewers, requiredCount, request.getNumber());
			throw new ExplicitException(errorMessage);
		}
	}
	
	private Predicate[] getPredicates(@Nullable Project targetProject, @Nullable Criteria<PullRequest> criteria, 
			CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (targetProject != null) {
			predicates.add(builder.equal(from.get(PullRequest.PROP_TARGET_PROJECT), targetProject));
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new ReadCode());
			if (!projects.isEmpty()) {
				Path<Long> projectIdPath = from.get(PullRequest.PROP_TARGET_PROJECT).get(Project.PROP_ID);
				predicates.add(Criteria.forManyValues(builder, projectIdPath, 
						projects.stream().map(it->it.getId()).collect(Collectors.toSet()), 
						projectManager.getIds()));
			} else {
				predicates.add(builder.disjunction());
			}
		}
		
		if (criteria != null) 
			predicates.add(criteria.getPredicate(query, from, builder));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<PullRequest> buildCriteriaQuery(Session session, @Nullable Project targetProject, 
			EntityQuery<PullRequest> requestQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<PullRequest> query = builder.createQuery(PullRequest.class);
		query.distinct(true);
		Root<PullRequest> root = query.from(PullRequest.class);
		
		query.where(getPredicates(targetProject, requestQuery.getCriteria(), query, root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: requestQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING) {
				orders.add(builder.asc(PullRequestQuery.getPath(
						root, PullRequest.ORDER_FIELDS.get(sort.getField()))));
			} else {
				orders.add(builder.desc(PullRequestQuery.getPath(
						root, PullRequest.ORDER_FIELDS.get(sort.getField()))));
			}
		}

		if (orders.isEmpty()) 
			orders.add(builder.desc(PullRequestQuery.getPath(root, PullRequest.PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE)));
		
		query.orderBy(orders);
		
		return query;
	}

	@Sessional
	@Override
	public List<PullRequest> query(@Nullable Project targetProject, EntityQuery<PullRequest> requestQuery, 
			boolean loadReviewsAndBuilds, int firstResult, int maxResults) {
		CriteriaQuery<PullRequest> criteriaQuery = buildCriteriaQuery(getSession(), targetProject, requestQuery);
		Query<PullRequest> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);

		List<PullRequest> requests = query.getResultList();
		if (!requests.isEmpty() && loadReviewsAndBuilds) {
			reviewManager.populateReviews(requests);
			buildManager.populateBuilds(requests);
		}
		
		return requests;
	}
	
	@Sessional
	@Override
	public int count(@Nullable Project targetProject,  Criteria<PullRequest> requestCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		criteriaQuery.where(getPredicates(targetProject, requestCriteria, criteriaQuery, root, builder));

		criteriaQuery.select(builder.countDistinct(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}
	
	@Sessional
	@Override
	public PullRequest findOpen(ProjectAndBranch target, ProjectAndBranch source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofTarget(target)).add(ofSource(source)).add(ofOpen());
		return find(criteria);
	}
	
	@Sessional
	@Override
	public PullRequest find(Project targetProject, long number) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq(PullRequest.PROP_NUMBER_SCOPE, targetProject.getForkRoot()));
		criteria.add(Restrictions.eq(PullRequest.PROP_NUMBER, number));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Sessional
	public PullRequest findByUUID(String uuid) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq(PullRequest.PROP_UUID, uuid));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Sessional
	@Override
	public PullRequest findByFQN(String pullRequestFQN) {
		return find(ProjectScopedNumber.from(pullRequestFQN));
	}
	
	@Sessional
	@Override
	public PullRequest find(ProjectScopedNumber pullRequestFQN) {
		return find(pullRequestFQN.getProject(), pullRequestFQN.getNumber());
	}
	
	@Sessional
	@Override
	public List<PullRequest> query(Project project, String term, int count) {
		List<PullRequest> requests = new ArrayList<>();

		EntityCriteria<PullRequest> criteria = newCriteria();

		if (term.contains("#")) {
			String projectPath = StringUtils.substringBefore(term, "#");
			Project specifiedProject = projectManager.findByPath(projectPath);
			if (specifiedProject != null && SecurityUtils.canAccess(specifiedProject)) {
				project = specifiedProject;
				term = StringUtils.substringAfter(term, "#");
			}
		}
		
		Set<Project> projects = Sets.newHashSet(project);
		projects.addAll(project.getForkParents().stream().filter(it->SecurityUtils.canReadCode(it)).collect(Collectors.toSet()));
		criteria.add(Restrictions.in(PullRequest.PROP_TARGET_PROJECT, projects));
		
		if (term.startsWith("#"))
			term = term.substring(1);
		if (term.length() != 0) {
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq(PullRequest.PROP_NUMBER, buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike(PullRequest.PROP_TITLE, term, MatchMode.ANYWHERE),
						Restrictions.ilike(PullRequest.PROP_NO_SPACE_TITLE, term, MatchMode.ANYWHERE)));
			}
		}
		
		criteria.addOrder(Order.desc(PullRequest.PROP_TARGET_PROJECT));
		criteria.addOrder(Order.desc(PullRequest.PROP_NUMBER));
		requests.addAll(query(criteria, 0, count));
		
		return requests;
	}

	@Transactional
	@Override
	public void delete(Collection<PullRequest> requests) {
		for (PullRequest request: requests)
			delete(request);
	}

	@Transactional
	@Override
	public void saveDescription(PullRequest request, @Nullable String description) {
		String prevDescription = request.getDescription();
		if (!Objects.equal(description, prevDescription)) {
			if (description != null && description.length() > PullRequest.MAX_DESCRIPTION_LEN)
				throw new ExplicitException("Description too long"); 
			request.setDescription(description);
			referenceManager.addReferenceChange(request, description);
			save(request);
		}
	}

	@Sessional
	@Override
	public List<ProjectPullRequestStats> queryStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectPullRequestStats> criteriaQuery = builder.createQuery(ProjectPullRequestStats.class);
			Root<PullRequest> root = criteriaQuery.from(PullRequest.class);
			criteriaQuery.multiselect(
					root.get(PullRequest.PROP_TARGET_PROJECT).get(Project.PROP_ID), 
					root.get(PullRequest.PROP_STATUS), builder.count(root));
			criteriaQuery.groupBy(root.get(PullRequest.PROP_TARGET_PROJECT), root.get(PullRequest.PROP_STATUS));
			
			criteriaQuery.where(root.get(PullRequest.PROP_TARGET_PROJECT).in(projects));
			criteriaQuery.orderBy(builder.asc(root.get(PullRequest.PROP_STATUS)));
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	private Criterion ofOpen() {
		return Restrictions.eq(PullRequest.PROP_STATUS, Status.OPEN);
	}
	
	private Criterion ofTarget(ProjectAndBranch target) {
		return Restrictions.and(
				Restrictions.eq(PullRequest.PROP_TARGET_PROJECT, target.getProject()),
				Restrictions.eq(PullRequest.PROP_TARGET_BRANCH, target.getBranch()));
	}

	private Criterion ofTargetProject(Project target) {
		return Restrictions.eq(PullRequest.PROP_TARGET_PROJECT, target);
	}
	
	private Criterion ofSource(ProjectAndBranch source) {
		return Restrictions.and(
				Restrictions.eq(PullRequest.PROP_SOURCE_PROJECT, source.getProject()),
				Restrictions.eq(PullRequest.PROP_SOURCE_BRANCH, source.getBranch()));
	}
	
	private Criterion ofSourceProject(Project source) {
		return Restrictions.eq(PullRequest.PROP_SOURCE_PROJECT, source);
	}
	
}
