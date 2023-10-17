package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.project.pullrequest.*;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.xodus.CommitInfoManager;
import io.onedev.server.xodus.PullRequestInfoManager;
import io.onedev.server.model.*;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.FileProtection;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.*;
import io.onedev.server.persistence.SequenceGenerator;
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
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.server.model.PullRequest.getSerialLockName;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.*;

@Singleton
public class DefaultPullRequestManager extends BaseEntityManager<PullRequest> 
		implements PullRequestManager, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private final PullRequestUpdateManager updateManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestReviewManager reviewManager;
	
	private final BuildManager buildManager;
	
	private final CommitInfoManager commitInfoManager;

	private final PullRequestChangeManager changeManager;
	
	private final TransactionManager transactionManager;
	
	private final PendingSuggestionApplyManager pendingSuggestionApplyManager;
	
	private final GitService gitService;
	
	private final PullRequestInfoManager pullRequestInfoManager;
	
	private final SequenceGenerator numberGenerator;
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager updateManager, 
									 PullRequestReviewManager reviewManager, ListenerRegistry listenerRegistry, 
									 PullRequestChangeManager changeManager, BuildManager buildManager, 
									 TransactionManager transactionManager, ProjectManager projectManager, 
									 CommitInfoManager commitInfoManager, ClusterManager clusterManager, 
									 UserManager userManager, GitService gitService, 
									 PendingSuggestionApplyManager pendingSuggestionApplyManager, 
									 PullRequestInfoManager pullRequestInfoManager) {
		super(dao);
		
		this.updateManager = updateManager;
		this.reviewManager = reviewManager;
		this.transactionManager = transactionManager;
		this.listenerRegistry = listenerRegistry;
		this.changeManager = changeManager;
		this.buildManager = buildManager;
		this.projectManager = projectManager;
		this.commitInfoManager = commitInfoManager;
		this.pendingSuggestionApplyManager = pendingSuggestionApplyManager;
		this.userManager = userManager;
		this.gitService = gitService;
		this.pullRequestInfoManager = pullRequestInfoManager;
		
		numberGenerator = new SequenceGenerator(PullRequest.class, clusterManager, dao);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestManager.class);
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		doDelete(request);
		listenerRegistry.post(new PullRequestDeleted(request));
	}
	
	private void doDelete(PullRequest request) {
		Collection<String> refs = Lists.newArrayList(
				request.getHeadRef(), request.getMergeRef(), request.getBaseRef(), request.getBuildRef());
		for (PullRequestUpdate update: request.getUpdates())
			refs.add(update.getHeadRef());
		
		gitService.deleteRefs(request.getTargetProject(), refs);
		
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
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}

	@Transactional
	@Override
	public void restoreSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null);
		if (request.getSource().getObjectName(false) == null) {
			getGitService().createBranch(request.getSourceProject(), request.getSourceBranch(), 
					request.getLatestUpdate().getHeadCommitHash());
			
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setData(new PullRequestSourceBranchRestoreData());
			change.setRequest(request);
			change.setUser(SecurityUtils.getUser());
			changeManager.create(change, note);
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
			changeManager.create(change, note);
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
		changeManager.create(change, note);
		
		MergePreview mergePreview = request.checkMergePreview();
		if (mergePreview != null)
			updateMergePreviewRef(request);
		
		checkAsync(request, false, true);
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
		changeManager.create(change, note);
		
		pendingSuggestionApplyManager.discard(null, request);
	}
	
	@Transactional
	@Override
	public void merge(PullRequest request, String commitMessage) {
		MergePreview mergePreview = Preconditions.checkNotNull(request.checkMergePreview());
		ObjectId mergeCommitId = ObjectId.fromString(
				Preconditions.checkNotNull(mergePreview.getMergeCommitHash()));
		User user = SecurityUtils.getUser();
        PersonIdent person = user.asPerson();
        
		Project project = request.getTargetProject();
		MergeStrategy mergeStrategy = mergePreview.getMergeStrategy();
		
		if (mergeStrategy == CREATE_MERGE_COMMIT
				|| mergeStrategy == SQUASH_SOURCE_BRANCH_COMMITS
				|| mergeStrategy == CREATE_MERGE_COMMIT_IF_NECESSARY 
						&& !mergeCommitId.name().equals(mergePreview.getHeadCommitHash())) {
			PersonIdent author;
			if (mergeStrategy != SQUASH_SOURCE_BRANCH_COMMITS)
				author = person;
			else
				author = null;
			mergeCommitId = getGitService().amendCommit(project, mergeCommitId, 
					author, person, commitMessage);
		} else if (mergeStrategy == REBASE_SOURCE_BRANCH_COMMITS) {
			ObjectId targetHeadCommitId = ObjectId.fromString(mergePreview.getTargetHeadCommitHash());
			mergeCommitId = getGitService().amendCommits(project, mergeCommitId, targetHeadCommitId, 
					User.SYSTEM_NAME, person);
		}
		
		if (!mergeCommitId.name().equals(mergePreview.getMergeCommitHash())) {
			MergePreview lastMergePreview = mergePreview;
	        mergePreview = new MergePreview();
			mergePreview.setTargetHeadCommitHash(lastMergePreview.getTargetHeadCommitHash());
			mergePreview.setHeadCommitHash(lastMergePreview.getHeadCommitHash());
			mergePreview.setMergeStrategy(mergeStrategy);
			mergePreview.setMergeCommitHash(mergeCommitId.name());
	        request.setMergePreview(mergePreview);
			
			gitService.updateRef(request.getTargetProject(), request.getMergeRef(), 
					ObjectId.fromString(request.getMergePreview().getMergeCommitHash()), null);
		}
		
		closeAsMerged(request, false);

		gitService.updateRef(project, request.getTargetRef(), mergeCommitId, 
				ObjectId.fromString(mergePreview.getTargetHeadCommitHash()));
	}
	
	@Transactional
	@Override
	public void open(PullRequest request) {
		Preconditions.checkArgument(request.isNew());
		
		request.setNumberScope(request.getTargetProject().getForkRoot());
		request.setNumber(numberGenerator.getNextSequence(request.getNumberScope()));
		request.setSubmitDate(new Date());
		for (var update: request.getUpdates())
			update.setDate(new DateTime().plusMillis(1).toDate());
		
		LastActivity lastActivity = new LastActivity();
		lastActivity.setUser(request.getSubmitter());
		lastActivity.setDescription("opened");
		lastActivity.setDate(request.getSubmitDate());
		request.setLastActivity(lastActivity);
		
		dao.persist(request);

		gitService.updateRef(request.getTargetProject(), request.getBaseRef(), 
				ObjectId.fromString(request.getBaseCommitHash()), null);
		gitService.updateRef(request.getTargetProject(), request.getHeadRef(), 
				ObjectId.fromString(request.getLatestUpdate().getHeadCommitHash()), null);
		
		for (PullRequestUpdate update: request.getUpdates())
			updateManager.create(update);

		for (PullRequestReview review: request.getReviews())
			dao.persist(review);
		
		for (PullRequestAssignment assignment: request.getAssignments())
			dao.persist(assignment);

		MergePreview mergePreview = request.checkMergePreview();
		
		if (mergePreview != null) 
			updateMergePreviewRef(request);
		
		checkAsync(request, false, true);
		
		listenerRegistry.post(new PullRequestOpened(request));
	}
	
	private void closeAsMerged(PullRequest request, boolean dueToMerged) {
		request.setStatus(Status.MERGED);
		
		Date date = new DateTime().plusMillis(1).toDate();
		
		if (dueToMerged)
			request.setMergePreview(null);
			
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
		changeManager.create(change, null);
		
		pendingSuggestionApplyManager.discard(null, request);
	}
	
	private void updateMergePreviewRef(PullRequest request) {
		Project project = request.getTargetProject();
		MergePreview preview = request.getMergePreview();
		if (preview != null) {
			ObjectId mergedId = preview.getMergeCommitHash() != null ?
					ObjectId.fromString(preview.getMergeCommitHash()) : null;
			if (mergedId != null && !mergedId.equals((project.getObjectId(request.getMergeRef(), false)))) {
				gitService.updateRef(project, request.getMergeRef(), mergedId, null);
			} else if (mergedId == null && project.getObjectId(request.getMergeRef(), false) != null) {
				gitService.deleteRefs(project, Sets.newHashSet(request.getMergeRef()));
			}
		} else if (project.getObjectId(request.getMergeRef(), false) != null) {
			gitService.deleteRefs(project, Sets.newHashSet(request.getMergeRef()));
		}
	}
	
	@Sessional
	@Listen
	public void updateBuildCommitRef(PullRequest request) {
		Project project = request.getTargetProject();
		ObjectId buildCommitId = null;
		if (request.getBuildCommitHash() != null)
			buildCommitId = ObjectId.fromString(request.getBuildCommitHash());
		if (buildCommitId != null && !buildCommitId.equals((project.getObjectId(request.getBuildRef(), false)))) {
			gitService.updateRef(project, request.getBuildRef(), buildCommitId, null);
		} else if (buildCommitId == null
				&& project.getObjectId(request.getBuildRef(), false) != null) {
			gitService.deleteRefs(project, Sets.newHashSet(request.getBuildRef()));
		}
	}

	private void gitDirModified(Long projectId) {
		var gitDir = projectManager.getRepository(projectId).getDirectory();
		projectManager.directoryModified(projectId, gitDir);	
	}
	
	@Listen
	public void on(PullRequestOpened event) {
		gitDirModified(event.getProject().getId());
	}

	@Listen
	public void on(PullRequestUpdated event) {
		gitDirModified(event.getProject().getId());
	}

	@Listen
	public void on(PullRequestDeleted event) {
		gitDirModified(event.getProject().getId());
	}

	@Listen
	public void on(PullRequestsDeleted event) {
		gitDirModified(event.getProject().getId());
	}

	@Sessional
	@Listen
	public void on(PullRequestMergePreviewUpdated event) {
		gitDirModified(event.getProject().getId());
	}

	@Sessional
	@Listen
	public void on(PullRequestBuildCommitUpdated event) {
		gitDirModified(event.getProject().getId());
	}

	private void check(PullRequest request, boolean sourceUpdated, boolean updateBuildCommit) {
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
						
						for (PullRequestReview review: request.getReviews()) {
							if (review.isNew())
								reviewManager.create(review);
							else if (review.isDirty())
								reviewManager.update(review);
						}

						Project targetProject = request.getTargetProject();
						if (request.isOpen() && !request.isMergedIntoTarget()) {
							MergePreview mergePreview = request.checkMergePreview();
							if (mergePreview == null) {
								mergePreview = new MergePreview();
								mergePreview.setTargetHeadCommitHash(request.getTarget().getObjectName());
								mergePreview.setHeadCommitHash(request.getLatestUpdate().getHeadCommitHash());
								mergePreview.setMergeStrategy(request.getMergeStrategy());
								logger.debug("Calculating merge preview of pull request #{} in project '{}'...", 
										request.getNumber(), targetProject.getPath());
								ObjectId merged = mergePreview.getMergeStrategy()
										.merge(request, "Merge preview of pull request #" + request.getNumber());
								if (merged != null)
									mergePreview.setMergeCommitHash(merged.name());
								
								request.setMergePreview(mergePreview);

								if (updateBuildCommit) {
									request.setBuildCommitHash(mergePreview.getMergeCommitHash());
									updateBuildCommitRef(request);
									listenerRegistry.post(new PullRequestBuildCommitUpdated((request)));
								}
								dao.persist(request);
								updateMergePreviewRef(request);
								listenerRegistry.post(new PullRequestMergePreviewUpdated(request));
							} else if (updateBuildCommit && 
									!Objects.equals(mergePreview.getMergeCommitHash(), request.getBuildCommitHash())) {
								request.setBuildCommitHash(mergePreview.getMergeCommitHash());
								dao.persist(request);
								updateBuildCommitRef(request);
								listenerRegistry.post(new PullRequestBuildCommitUpdated(request));
							}
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
					request.getTargetProject().getPath(), request.getNumber());
			listenerRegistry.post(new PullRequestCheckFailed(request));
			logger.error(message, e);
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	if (project.getForkRoot().equals(project))
	    		numberGenerator.removeNextSequence(project);
		}
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		if (branch != null && !event.getOldCommitId().equals(ObjectId.zeroId())) {
			ProjectAndBranch projectAndBranch = new ProjectAndBranch(event.getProject(), branch);
			Criterion criterion = Restrictions.and(
					ofOpen(), 
					Restrictions.or(ofSource(projectAndBranch), ofTarget(projectAndBranch)));
			for (PullRequest request: query(EntityCriteria.of(PullRequest.class).add(criterion))) {
				boolean sourceUpdated = request.getSource() != null 
						&& request.getSource().equals(projectAndBranch);
				checkAsync(request, sourceUpdated, sourceUpdated);
			}
		}
	}

	@Sessional
	@Override
	public PullRequest findEffective(ProjectAndBranch target, ProjectAndBranch source) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Criterion merged = Restrictions.and(
				Restrictions.eq(PullRequest.PROP_STATUS, Status.MERGED), 
				Restrictions.eq(PullRequest.PROP_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH, source.getObjectName()));
		
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
					Restrictions.eq(PullRequest.PROP_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH, source.getObjectName()));
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
		if (data instanceof PullRequestApproveData || data instanceof PullRequestDiscardData) {
			check(event.getRequest(), false, false);
		} else if (data instanceof PullRequestMergeStrategyChangeData
				|| data instanceof PullRequestTargetBranchChangeData) {
			check(event.getRequest(), false, true);
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestEvent event) {
		if (event instanceof PullRequestUpdated) {
			Collection<User> committers = ((PullRequestUpdated) event).getCommitters();
			if (committers.size() == 1) {
				LastActivity lastActivity = new LastActivity();
				lastActivity.setUser(committers.iterator().next());
				lastActivity.setDescription("added commits");
				lastActivity.setDate(event.getDate());
				event.getRequest().setLastActivity(lastActivity);
			} else {
				LastActivity lastActivity = new LastActivity();
				lastActivity.setDescription("Commits added");
				lastActivity.setDate(event.getDate());
				event.getRequest().setLastActivity(lastActivity);
			}
		} else if (!event.isMinor()) {
			event.getRequest().setLastActivity(event.getLastUpdate());
			if (event instanceof PullRequestCodeCommentEvent)
				event.getRequest().setCodeCommentsUpdateDate(event.getDate());
		}
	}
	
	@Listen
	@Sessional
	public void on(BuildEvent event) {
		Build build = event.getBuild();
		if (build.getRequest() != null) {
			if (build.getRequest().isDiscarded()) {
				if (build.getRequest().getLatestUpdate().getTargetHeadCommitHash().equals(build.getCommitHash()))
					listenerRegistry.post(new PullRequestBuildEvent(build));
			} else {
				MergePreview mergePreview = build.getRequest().checkMergePreview();
				if (mergePreview != null && build.getCommitHash().equals(mergePreview.getMergeCommitHash()))
					listenerRegistry.post(new PullRequestBuildEvent(build));
			}
		}
	}
	
	@Sessional
	public void checkAsync(PullRequest request, boolean sourceUpdated, boolean updateBuildCommit) {
		Long projectId = request.getTargetProject().getId();
		Long requestId = request.getId();
		
		transactionManager.runAfterCommit(new ClusterRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public void run() {
				projectManager.submitToActiveServer(projectId, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() {
						try {
							LockUtils.call(getSerialLockName(requestId), true, new ClusterTask<Void>() {

								private static final long serialVersionUID = 1L;

								@Override
								public Void call() {
									transactionManager.run(new ClusterRunnable() {

										private static final long serialVersionUID = 1L;

										@Override
										public void run() {
											PullRequest request = load(requestId);
											check(request, sourceUpdated, updateBuildCommit);
										}

									});
									return null;
								}

							});
						} catch (Exception e) {
							logger.error("Error checking pull request", e);
						}
						return null;
					}
					
				});
			}
			
		});
		
	}
	
	@Transactional
	@Override
	public void checkReviews(PullRequest request, boolean sourceUpdated) {
		BranchProtection branchProtection = request.getTargetProject()
				.getBranchProtection(request.getTargetBranch(), request.getSubmitter());
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
		
		ReviewRequirement checkedRequirement = ReviewRequirement.parse(null);
		
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
			Map<Long, Collection<EmailAddressFacade>> candidateReviewerEmails = new HashMap<>();
			for (User user: users) {
				if (request.getReview(user) == null) {
					var emailAddresses = user.getEmailAddresses().stream()
							.map(it->it.getFacade()).collect(Collectors.toList());
					candidateReviewerEmails.put(user.getId(), emailAddresses);
				}
			}
			
			for (Long reviewerId: commitInfoManager.sortUsersByContribution(
					candidateReviewerEmails, project.getId(), changedFiles)) {
				User user = userManager.load(reviewerId);
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
			orders.add(builder.desc(PullRequestQuery.getPath(root, PullRequest.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE)));
		
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

		criteriaQuery.select(builder.count(root));
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
	public void delete(Collection<PullRequest> requests, Project project) {
		for (PullRequest request: requests)
			doDelete(request);
		listenerRegistry.post(new PullRequestsDeleted(project, requests));
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

	@Sessional
	@Override
	public ObjectId getComparisonBase(PullRequest request, ObjectId oldCommitId, ObjectId newCommitId) {
		if (request.isNew() || oldCommitId.equals(newCommitId))
			return oldCommitId;
		
		Project targetProject = request.getTargetProject();
		ObjectId comparisonBase = pullRequestInfoManager.getComparisonBase(request, oldCommitId, newCommitId);
		if (comparisonBase != null && !getGitService().hasObjects(targetProject, comparisonBase))
			comparisonBase = null;
		if (comparisonBase == null) {
			for (PullRequestUpdate update: request.getSortedUpdates()) {
				if (update.getCommits().contains(newCommitId)) {
					ObjectId targetHead = ObjectId.fromString(update.getTargetHeadCommitHash());
					
					ObjectId mergeBase1 = getGitService().getMergeBase(targetProject, targetHead, targetProject, newCommitId);
					if (mergeBase1 != null) {
						ObjectId mergeBase2 = getGitService().getMergeBase(targetProject, mergeBase1, targetProject, oldCommitId);
						if (mergeBase2.equals(mergeBase1)) {
							comparisonBase = oldCommitId;
							break;
						} else if (mergeBase2.equals(oldCommitId)) {
							comparisonBase = mergeBase1;
							break;
						} else {
							PersonIdent person = new PersonIdent(User.SYSTEM_NAME, User.SYSTEM_EMAIL_ADDRESS);
							comparisonBase = getGitService().merge(targetProject, oldCommitId, mergeBase1, 
									false, person, person, "helper commit", true);
							break;
						}
					} else {
						return oldCommitId;
					}
				}
			}
			if (comparisonBase != null)
				pullRequestInfoManager.cacheComparisonBase(request, oldCommitId, newCommitId, comparisonBase);
			else
				throw new IllegalStateException();
		}
		return comparisonBase;
	}
	
}
