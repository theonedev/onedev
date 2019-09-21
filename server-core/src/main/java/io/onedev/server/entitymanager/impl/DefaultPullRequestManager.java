package io.onedev.server.entitymanager.impl;

import static io.onedev.server.model.PullRequest.CriterionHelper.ofOpen;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofSource;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofSourceProject;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofSubmitter;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofTarget;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofTargetProject;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.Permission;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.MatrixRunner;
import io.onedev.commons.utils.concurrent.Prioritized;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.trigger.BranchUpdateTrigger;
import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestBuildManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.entitymanager.PullRequestUpdateManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.FileChange;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.FileProtection;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeStrategyChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewerAddData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReviewerRemoveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestSourceBranchDeleteData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestSourceBranchRestoreData;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.PullRequestConstants;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.scriptidentity.JobIdentity;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.util.work.BatchWorkManager;
import io.onedev.server.util.work.BatchWorker;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int MAX_CONTRIBUTION_FILES = 100;
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final SessionManager sessionManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestReviewManager pullRequestReviewManager;
	
	private final PullRequestBuildManager pullRequestBuildManager;

	private final BatchWorkManager batchWorkManager;
	
	private final PullRequestChangeManager pullRequestChangeManager;
	
	private final TransactionManager transactionManager;
	
	private final JobManager jobManager;
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager,  
			PullRequestReviewManager pullRequestReviewManager, UserManager userManager, 
			MarkdownManager markdownManager, BatchWorkManager batchWorkManager, 
			ListenerRegistry listenerRegistry, SessionManager sessionManager,
			PullRequestChangeManager pullRequestChangeManager,
			PullRequestBuildManager pullRequestBuildManager, TransactionManager transactionManager, 
			JobManager jobManager, ProjectManager projectManager) {
		super(dao);
		
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.pullRequestReviewManager = pullRequestReviewManager;
		this.transactionManager = transactionManager;
		this.userManager = userManager;
		this.batchWorkManager = batchWorkManager;
		this.sessionManager = sessionManager;
		this.listenerRegistry = listenerRegistry;
		this.pullRequestChangeManager = pullRequestChangeManager;
		this.pullRequestBuildManager = pullRequestBuildManager;
		this.jobManager = jobManager;
		this.projectManager = projectManager;
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
				throw ExceptionUtils.unchecked(e);
			}
			request.getSourceProject().cacheObjectId(request.getSourceBranch(), latestCommit.copy());
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setData(new PullRequestSourceBranchRestoreData(note));
			change.setRequest(request);
			change.setUser(userManager.getCurrent());
			pullRequestChangeManager.save(change);
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
			change.setData(new PullRequestSourceBranchDeleteData(note));
			change.setRequest(request);
			change.setUser(userManager.getCurrent());
			pullRequestChangeManager.save(change);
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String note) {
		User user = userManager.getCurrent();
		request.setCloseInfo(null);

		PullRequestChange change = new PullRequestChange();
		change.setDate(new Date());
		change.setData(new PullRequestReopenData(note));
		change.setRequest(request);
		change.setUser(user);
		pullRequestChangeManager.save(change);
		checkAsync(Lists.newArrayList(request));
	}

	@Transactional
	@Override
 	public void discard(PullRequest request, String note) {
		User user = userManager.getCurrent();
		Date date = new Date();
		
		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setDate(date);
		closeInfo.setUser(user);
		closeInfo.setStatus(CloseInfo.Status.DISCARDED);
		request.setCloseInfo(closeInfo);
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(date);
		change.setData(new PullRequestDiscardData(note));
		change.setRequest(request);
		change.setUser(user);
		pullRequestChangeManager.save(change);
	}
	
	private void merge(PullRequest request) {
		MergePreview preview = Preconditions.checkNotNull(request.getMergePreview());
		String merged = Preconditions.checkNotNull(preview.getMerged());
		
		ObjectId mergedId = ObjectId.fromString(merged);
		RevCommit mergedCommit = request.getTargetProject().getRevCommit(mergedId, true);
		
        PersonIdent committer = new PersonIdent(OneDev.NAME, "");
        
		Project targetProject = request.getTargetProject();
		MergeStrategy strategy = request.getMergeStrategy();
		if ((strategy == CREATE_MERGE_COMMIT || strategy == CREATE_MERGE_COMMIT_IF_NECESSARY || strategy == SQUASH_SOURCE_BRANCH_COMMITS) 
				&& !preview.getMerged().equals(preview.getRequestHead()) 
				&& !mergedCommit.getFullMessage().equals(request.getCommitMessage())) {
			try (	RevWalk revWalk = new RevWalk(targetProject.getRepository());
					ObjectInserter inserter = targetProject.getRepository().newObjectInserter()) {
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
				throw ExceptionUtils.unchecked(e);
			}
	        preview = new MergePreview(preview.getTargetHead(), preview.getRequestHead(), 
	        		preview.getMergeStrategy(), merged);
	        request.setLastMergePreview(preview);
			RefUpdate refUpdate = GitUtils.getRefUpdate(targetProject.getRepository(), request.getMergeRef());
			refUpdate.setNewObjectId(mergedId);
			GitUtils.updateRef(refUpdate);
		}
		
		closeAsMerged(request, false);
		
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
		ObjectId newTargetHeadId = mergedId;
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				dao.getSessionManager().runAsync(new Runnable() {

					@Override
					public void run() {
						PullRequest request = load(requestId);
						request.getTargetProject().cacheObjectId(request.getTargetRef(), newTargetHeadId);
						listenerRegistry.post(new RefUpdated(request.getTargetProject(), targetRef, 
									targetHeadId, newTargetHeadId));
					}
					
				}, SecurityUtils.getSubject());
			}
		});
	}
	
	@Transactional
	@Override
	public void open(PullRequest request) {
		Preconditions.checkArgument(request.isNew());
		Query<?> query = getSession().createQuery("select max(number) from PullRequest where targetProject=:project");
		query.setParameter("project", request.getTargetProject());
		request.setNumber(getNextNumber(request.getTargetProject(), query));

		dao.persist(request);
		
		RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), request.getBaseRef());
		refUpdate.setNewObjectId(ObjectId.fromString(request.getBaseCommitHash()));
		GitUtils.updateRef(refUpdate);
		
		refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), request.getHeadRef());
		refUpdate.setNewObjectId(ObjectId.fromString(request.getHeadCommitHash()));
		GitUtils.updateRef(refUpdate);
		
		for (PullRequestUpdate update: request.getUpdates())
			pullRequestUpdateManager.save(update, false);
		
		pullRequestReviewManager.saveReviews(request);
		pullRequestBuildManager.savePullRequestBuilds(request);

		checkAsync(Lists.newArrayList(request));
		
		listenerRegistry.post(new PullRequestOpened(request));
	}
	
	private void closeAsMerged(PullRequest request, boolean dueToMerged) {
		Date date = new DateTime().plusMillis(1).toDate();
		
		if (dueToMerged)
			request.setLastMergePreview(null);
		
		CloseInfo closeInfo = new CloseInfo();
		closeInfo.setDate(date);
		closeInfo.setStatus(CloseInfo.Status.MERGED);
		request.setCloseInfo(closeInfo);
		
		String reason = null;
		if (dueToMerged)
			reason = "closed pull request as source branch is merged into target branch";
		
		PullRequestChange change = new PullRequestChange();
		change.setDate(date);
		change.setData(new PullRequestMergeData(reason));
		change.setRequest(request);
		pullRequestChangeManager.save(change);
	}

	private void checkUpdate(PullRequest request) {
		if (!request.getHeadCommitHash().equals(request.getSource().getObjectName())) {
			ObjectId mergeBase = GitUtils.getMergeBase(
					request.getTargetProject().getRepository(), request.getTarget().getObjectId(), 
					request.getSourceProject().getRepository(), request.getSource().getObjectId());
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
					checkUpdate(request);
					if (request.isMergeIntoTarget()) {
						closeAsMerged(request, true);
					} else {
						checkQuality(request);
						
						pullRequestReviewManager.saveReviews(request);
						pullRequestBuildManager.savePullRequestBuilds(request);
						
						MergePreview preview = request.getMergePreview();
						
						if (request.isAllReviewsApproved() 
								&& request.isAllBuildsSuccessful()
								&& request.getCheckError() == null 
								&& preview != null 
								&& preview.getMerged() != null) {
							merge(request);
						}
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
					request.getTargetProject().getName(), request.getNumber());
			logger.error(message, e);
		}
	}

	@Sessional
	@Override
	public MergePreview previewMerge(PullRequest request) {
		if (!request.isNew() && request.getMergeStrategy() != MergeStrategy.DO_NOT_MERGE) {
			MergePreview lastPreview = request.getLastMergePreview();
			if (request.isOpen() && !request.isMergeIntoTarget()) {
				if (lastPreview == null || !lastPreview.isUpToDate(request)) {
					int priority = RequestCycle.get() != null?UI_PREVIEW_PRIORITY:BACKEND_PREVIEW_PRIORITY;			
					Long requestId = request.getId();
					transactionManager.runAfterCommit(new Runnable() {
	
						@Override
						public void run() {
							batchWorkManager.submit(getMergePreviewer(requestId), new Prioritized(priority));
						}
						
					});
					return null;
				} else {
					lastPreview.syncRef(request);
					return lastPreview;
				}
			} else {
				return lastPreview;
			}
		} else {
			return null;
		}
	}
	
	private BatchWorker getMergePreviewer(Long requestId) {
		return new BatchWorker("request-" + requestId + "-previewMerge", 1) {

			@Override
			public void doWorks(Collection<Prioritized> works) {
				sessionManager.run(new Runnable() {

					@Override
					public void run() {
						Preconditions.checkState(works.size() == 1);
						PullRequest request = load(requestId);
						Project targetProject = request.getTargetProject();
						MergePreview mergePreview = request.getLastMergePreview();
						if (request.isOpen() && !request.isMergeIntoTarget()) {
							if (mergePreview == null || !mergePreview.isUpToDate(request)) {
								mergePreview = new MergePreview(request.getTarget().getObjectName(), 
										request.getHeadCommitHash(), request.getMergeStrategy(), null);
								logger.debug("Calculating merge preview of pull request #{} in project '{}'...", 
										request.getNumber(), targetProject.getName());
								ObjectId merged = mergePreview.getMergeStrategy().merge(request);
								if (merged != null)
									mergePreview.setMerged(merged.name());
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
			checkAsync(query(EntityCriteria.of(PullRequest.class).add(criterion)));
		}
	}

	@Sessional
	@Override
	public PullRequest findEffective(ProjectAndBranch target, ProjectAndBranch source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Criterion merged = Restrictions.and(
				Restrictions.eq("closeInfo.status", CloseInfo.Status.MERGED), 
				Restrictions.eq("lastMergePreview.requestHead", source.getObjectName()));
		
		criteria.add(ofTarget(target)).add(ofSource(source)).add(Restrictions.or(ofOpen(), merged));
		
		return find(criteria);
	}
	
	@Sessional
	@Override
	public PullRequest findLatest(Project targetProject, User submitter) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(ofOpen());
		criteria.add(Restrictions.or(ofSourceProject(targetProject), ofTargetProject(targetProject)));
		criteria.add(ofSubmitter(submitter));
		criteria.addOrder(Order.desc("id"));
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

	@Sessional
	@Override
	public int countOpen(Project targetProject) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(PullRequest.CriterionHelper.ofOpen());
		criteria.add(PullRequest.CriterionHelper.ofTargetProject(targetProject));
		return count(criteria);
	}

	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		PullRequestChangeData data = event.getChange().getData();
		if (data instanceof PullRequestApproveData || data instanceof PullRequestDiscardData  
				|| data instanceof PullRequestReviewerRemoveData || data instanceof PullRequestReviewerAddData 
				|| data instanceof PullRequestMergeStrategyChangeData) {
			checkAsync(Lists.newArrayList(event.getRequest()));
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (!(event instanceof PullRequestMergePreviewCalculated) && !(event instanceof PullRequestBuildEvent))
			event.getRequest().setUpdateDate(event.getDate());
	}
	
	@Transactional
	@Listen
	public void on(PullRequestCodeCommentEvent event) {
		event.getRequest().setLastCodeCommentActivityDate(event.getDate());
	}
	
	@Listen
	@Transactional
	public void on(BuildEvent event) {
		Build build = event.getBuild();
		for (PullRequestBuild pullRequestBuild: build.getPullRequestBuilds())  
			listenerRegistry.post(new PullRequestBuildEvent(pullRequestBuild));
		checkAsync(build.getPullRequestBuilds()
				.stream()
				.map(it->it.getRequest())
				.collect(Collectors.toList()));
	}
	
	@Listen
	public void on(PullRequestMergePreviewCalculated event) {
		checkAsync(Lists.newArrayList(event.getRequest()));
	}
	
	@Sessional
	protected void checkAsync(Collection<PullRequest> requests) {
		Collection<Long> requestIds = requests.stream().map(it->it.getId()).collect(Collectors.toList());
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				dao.getSessionManager().runAsync(new Runnable() {

					@Override
					public void run() {
				        for (Long requestId: requestIds)
				        	check(load(requestId));
					}
					
				}, SecurityUtils.getSubject());
			}
			
		});
	}
	
	@Transactional
	@Override
	public void checkQuality(PullRequest request) {
		BranchProtection branchProtection = request.getTargetProject().getBranchProtection(request.getTargetBranch(), request.getSubmitter());
		if (branchProtection != null) {
			checkReviews(ReviewRequirement.fromString(branchProtection.getReviewRequirement()), request.getLatestUpdate());

			Set<FileProtection> checkedFileProtections = new HashSet<>();
			for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
				if (checkedFileProtections.containsAll(branchProtection.getFileProtections()))
					break;
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				for (String file: update.getChangedFiles()) {
					FileProtection fileProtection = branchProtection.getFileProtection(file);
					if (fileProtection != null  
							&& !checkedFileProtections.contains(fileProtection)) {
						checkedFileProtections.add(fileProtection);
						checkReviews(ReviewRequirement.fromString(fileProtection.getReviewRequirement()), update);
					}
				}
			}
		}

		checkBuilds(request);
		
		ProjectFacade project = request.getTargetProject().getFacade();
		Permission writeCode = new ProjectPermission(project, ProjectPrivilege.CODE_WRITE); 
		if (request.getSubmitter() == null || !request.getSubmitter().asSubject().isPermitted(writeCode)) {
			Collection<User> writers = new ArrayList<>();
			for (UserFacade facade: SecurityUtils.getAuthorizedUsers(project, ProjectPrivilege.CODE_WRITE)) 
				writers.add(userManager.load(facade.getId()));
			checkReviews(writers, 1, request.getLatestUpdate());
		}
	}

	private void checkBuilds(PullRequest request) {
		Collection<PullRequestBuild> prevRequirements = new ArrayList<>(request.getPullRequestBuilds());
		request.getPullRequestBuilds().clear();
		MergePreview preview = request.getMergePreview();
		if (preview != null && preview.getMerged() != null) {
			Project project = request.getTargetProject();
			ObjectId commitId = ObjectId.fromString(preview.getMerged());
			ScriptIdentity.push(new JobIdentity(project, commitId));
			try {
				CISpec ciSpec = project.getCISpec(commitId);
				if (ciSpec != null) {
					for (Job job: ciSpec.getJobs()) {
						for (JobTrigger trigger: job.getTriggers()) {
							if (trigger instanceof BranchUpdateTrigger) {
								BranchUpdateTrigger branchUpdateTrigger = (BranchUpdateTrigger) trigger;
								if (branchUpdateTrigger.isRejectIfNotSuccessful()) {
									RefUpdated updated = new RefUpdated(project, GitUtils.branch2ref(request.getTargetBranch()), 
											request.getTarget().getObjectId(), commitId);
									if (branchUpdateTrigger.matches(updated, job)) {
										new MatrixRunner<List<String>>(JobParam.getParamMatrix(trigger.getParams())) {
											
											@Override
											public void run(Map<String, List<String>> paramMap) {
												Build build = jobManager.submit(request.getTargetProject(), 
														commitId, job.getName(), paramMap, null);
												PullRequestBuild pullRequestBuild = null;
												for (PullRequestBuild prevRequirement: prevRequirements) {
													if (prevRequirement.getBuild().equals(build)) {
														pullRequestBuild = prevRequirement;
														break;
													}
												}
												if (pullRequestBuild == null) {
													pullRequestBuild = new PullRequestBuild();
													pullRequestBuild.setRequest(request);
													pullRequestBuild.setBuild(build);
												}
												request.getPullRequestBuilds().add(pullRequestBuild);
											}
											
										}.run();
									}
								}
							}
						}
					}
				}
			} finally {
				ScriptIdentity.pop();
			}
		}
	}
	
	private void checkReviews(ReviewRequirement reviewRequirement, PullRequestUpdate update) {
		PullRequest request = update.getRequest();
		
		for (User user: reviewRequirement.getUsers()) {
			if (!user.equals(request.getSubmitter())) {
				PullRequestReview review = request.getReview(user);
				if (review == null) {
					review = new PullRequestReview();
					review.setRequest(request);
					review.setUser(user);
					request.getReviews().add(review);
				} else if (review.getExcludeDate() != null) {
					review.setExcludeDate(null);
				} else if (review.getUpdate() == null || review.getUpdate().getId()<update.getId()) {
					review.setResult(null);
				}
			}
		}
		
		for (Map.Entry<Group, Integer> entry: reviewRequirement.getGroups().entrySet()) {
			Group group = entry.getKey();
			int requiredCount = entry.getValue();
			checkReviews(group.getMembers(), requiredCount, update);
		}
	}
	
	private void checkReviews(Collection<User> users, int requiredCount, PullRequestUpdate update) {
		PullRequest request = update.getRequest();

		if (requiredCount == 0)
			requiredCount = users.size();
		
		int effectiveCount = 0;
		Set<User> potentialReviewers = new HashSet<>();
		for (User user: users) {
			if (user.equals(request.getSubmitter())) {
				effectiveCount++;
			} else {
				PullRequestReview review = request.getReview(user);
				if (review != null && review.getExcludeDate() == null) {
					if (review.getResult() == null)
						requiredCount--;
					else if (review.getUpdate() != null && review.getUpdate().getId()>=update.getId())
						effectiveCount++;
					else
						potentialReviewers.add(user);
				} else {
					potentialReviewers.add(user);
				}
			} 
			if (effectiveCount >= requiredCount)
				break;
		}

		int missingCount = requiredCount - effectiveCount;
		Set<User> reviewers = new HashSet<>();
		
		List<User> candidateReviewers = new ArrayList<>();
		for (User user: potentialReviewers) {
			PullRequestReview review = request.getReview(user);
			if (review != null && review.getExcludeDate() == null)
				candidateReviewers.add(user);
		}
		
		sortByContributions(candidateReviewers, update);
		
		for (User user: candidateReviewers) {
			reviewers.add(user);
			request.getReview(user).setResult(null);
			if (reviewers.size() == missingCount)
				break;
		}
		
		if (reviewers.size() < missingCount) {
			candidateReviewers = new ArrayList<>();
			for (User user: potentialReviewers) {
				PullRequestReview review = request.getReview(user);
				if (review == null) 
					candidateReviewers.add(user);
			}
			sortByContributions(candidateReviewers, update);
			for (User user: candidateReviewers) {
				reviewers.add(user);
				PullRequestReview review = new PullRequestReview();
				review.setRequest(request);
				review.setUser(user);
				request.getReviews().add(review);
				if (reviewers.size() == missingCount)
					break;
			}
		}
		
		if (reviewers.size() < missingCount) {
			List<PullRequestReview> excludedReviews = new ArrayList<>();
			for (User user: potentialReviewers) {
				PullRequestReview review = request.getReview(user);
				if (review != null && review.getExcludeDate() != null)
					excludedReviews.add(review);
			}
			excludedReviews.sort(Comparator.comparing(PullRequestReview::getExcludeDate));
			
			for (PullRequestReview review: excludedReviews) {
				reviewers.add(review.getUser());
				review.setExcludeDate(null);
				if (reviewers.size() == missingCount)
					break;
			}
		}
		if (reviewers.size() < missingCount) {
			String errorMessage = String.format("Impossible to provide required number of reviewers "
					+ "(candidates: %s, required number of reviewers: %d, pull request: #%d)", 
					reviewers, missingCount, update.getRequest().getNumber());
			throw new OneException(errorMessage);
		}
	}
	
	private void sortByContributions(List<User> users, PullRequestUpdate update) {
		if (users.size() <= 1)
			return;
		
		CommitInfoManager commitInfoManager = OneDev.getInstance(CommitInfoManager.class);
		Map<User, Long> contributions = new HashMap<>();
		for (User user: users)
			contributions.put(user, 0L);

		int count = 0;
		for (FileChange change: update.getFileChanges()) {
			String path = change.getPath();
			int edits = change.getAdditions() + change.getDeletions();
			if (edits < 0)
				edits = 100;
			else if (edits == 0)
				edits = 1;
			int addedContributions = addContributions(contributions, commitInfoManager, path, edits, update);
			while (addedContributions == 0) {
				if (path.contains("/")) {
					path = StringUtils.substringBeforeLast(path, "/");
					addedContributions = addContributions(contributions, commitInfoManager, path, edits, update);
				} else {
					addContributions(contributions, commitInfoManager, "", edits, update);
					break;
				}
			}
			if (++count >= MAX_CONTRIBUTION_FILES)
				break;
		}

		Collections.sort(users, new Comparator<User>() {

			@Override
			public int compare(User o1, User o2) {
				if (contributions.get(o1) < contributions.get(o2))
					return 1;
				else
					return -1;
			}
			
		});
	}
	
	private int addContributions(Map<User, Long> contributions, CommitInfoManager commitInfoManager, 
			String path, int edits, PullRequestUpdate update) {
		int addedContributions = 0;
		for (Map.Entry<User, Long> entry: contributions.entrySet()) {
			User user = entry.getKey();
			int pathEdits = commitInfoManager.getEdits(
					update.getRequest().getTargetProject().getFacade(), user.getFacade(), path);
			entry.setValue(entry.getValue() + edits*pathEdits);
			addedContributions += pathEdits;
		}
		return addedContributions;
	}
	
	private Predicate[] getPredicates(io.onedev.server.search.entity.EntityCriteria<PullRequest> criteria, 
			Project targetProject, User user, Root<PullRequest> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("targetProject"), targetProject));
		if (criteria != null)
			predicates.add(criteria.getPredicate(targetProject, root, builder, user));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<PullRequest> buildCriteriaQuery(Session session, Project targetProject, User user,
			EntityQuery<PullRequest> requestQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<PullRequest> query = builder.createQuery(PullRequest.class);
		Root<PullRequest> root = query.from(PullRequest.class);
		query.select(root).distinct(true);
		
		query.where(getPredicates(requestQuery.getCriteria(), targetProject, user, root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: requestQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(PullRequestQuery.getPath(root, PullRequestConstants.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(PullRequestQuery.getPath(root, PullRequestConstants.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get("number")));
		query.orderBy(orders);
		
		return query;
	}

	@Sessional
	@Override
	public List<PullRequest> query(Project targetProject, User user, EntityQuery<PullRequest> requestQuery, int firstResult, int maxResults) {
		CriteriaQuery<PullRequest> criteriaQuery = buildCriteriaQuery(getSession(), targetProject, user, requestQuery);
		Query<PullRequest> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}
	
	@Sessional
	@Override
	public int count(Project targetProject, User user, io.onedev.server.search.entity.EntityCriteria<PullRequest> requestCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		criteriaQuery.where(getPredicates(requestCriteria, targetProject, user, root, builder));

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
	
	@Transactional
	@Override
	public PullRequest open(ProjectAndBranch source, ProjectAndBranch target, MergeStrategy mergeStrategy, 
			User submitter, String title) {
		ObjectId baseCommitId = GitUtils.getMergeBase(
				target.getProject().getRepository(), target.getObjectId(), 
				source.getProject().getRepository(), source.getObjectId());
		if (baseCommitId != null) {
			PullRequest request = new PullRequest();
			request.setTarget(target);
			request.setSource(source);
			request.setSubmitter(submitter);
			request.setTitle(title);
			request.setBaseCommitHash(baseCommitId.name());
			request.setHeadCommitHash(source.getObjectName());
			PullRequestUpdate update = new PullRequestUpdate();
			request.addUpdate(update);
			update.setRequest(request);
			update.setHeadCommitHash(request.getHeadCommitHash());
			update.setMergeBaseCommitHash(request.getBaseCommitHash());
			request.setMergeStrategy(mergeStrategy);
			open(request);
			
			return request;
		} else {
			throw new OneException("No merge base found");
		}
	}
	
	@Sessional
	@Override
	public PullRequest find(Project targetProject, long number) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("targetProject", targetProject));
		criteria.add(Restrictions.eq("number", number));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<PullRequest> query(Project targetProject, String term, int count) {
		List<PullRequest> requests = new ArrayList<>();

		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("targetProject", targetProject));
		
		if (StringUtils.isNotBlank(term)) {
			if (term.startsWith("#")) {
				term = term.substring(1);
				try {
					long buildNumber = Long.parseLong(term);
					criteria.add(Restrictions.eq("number", buildNumber));
				} catch (NumberFormatException e) {
					criteria.add(Restrictions.or(
							Restrictions.ilike("title", "#" + term, MatchMode.ANYWHERE),
							Restrictions.ilike("noSpaceTitle", "#" + term, MatchMode.ANYWHERE)));
				}
			} else try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("title", term, MatchMode.ANYWHERE),
						Restrictions.ilike("noSpaceTitle", term, MatchMode.ANYWHERE)));
			}
		}
		
		criteria.addOrder(Order.desc("number"));
		requests.addAll(query(criteria, 0, count));
		
		return requests;
	}

}
