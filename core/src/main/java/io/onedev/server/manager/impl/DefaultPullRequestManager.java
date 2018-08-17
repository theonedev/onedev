package io.onedev.server.manager.impl;

import static io.onedev.server.model.PullRequest.CriterionHelper.ofOpen;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofSource;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofSourceProject;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofSubmitter;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofTarget;
import static io.onedev.server.model.PullRequest.CriterionHelper.ofTargetProject;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.ALWAYS_MERGE;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.MERGE_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_MERGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.Permission;
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
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import io.onedev.launcher.loader.Listen;
import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.entityquery.EntityQuery;
import io.onedev.server.entityquery.EntitySort;
import io.onedev.server.entityquery.EntitySort.Direction;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.entityquery.pullrequest.PullRequestQuery;
import io.onedev.server.entityquery.pullrequest.PullRequestQueryBuildContext;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.pullrequest.PullRequestActionEvent;
import io.onedev.server.event.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.pullrequest.PullRequestMergePreviewCalculated;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.exception.OneException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.FileChange;
import io.onedev.server.manager.BatchWorkManager;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.CommitInfoManager;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.manager.MarkdownManager;
import io.onedev.server.manager.PullRequestActionManager;
import io.onedev.server.manager.PullRequestBuildManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.PullRequestReviewManager;
import io.onedev.server.manager.PullRequestUpdateManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.PullRequestBuild;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.FileProtection;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.ProjectAndBranch;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;
import io.onedev.server.model.support.pullrequest.actiondata.ActionData;
import io.onedev.server.model.support.pullrequest.actiondata.AddedReviewerData;
import io.onedev.server.model.support.pullrequest.actiondata.ApprovedData;
import io.onedev.server.model.support.pullrequest.actiondata.ChangedMergeStrategyData;
import io.onedev.server.model.support.pullrequest.actiondata.DeletedSourceBranchData;
import io.onedev.server.model.support.pullrequest.actiondata.DiscardedData;
import io.onedev.server.model.support.pullrequest.actiondata.MergedData;
import io.onedev.server.model.support.pullrequest.actiondata.RemovedReviewerData;
import io.onedev.server.model.support.pullrequest.actiondata.ReopenedData;
import io.onedev.server.model.support.pullrequest.actiondata.RestoredSourceBranchData;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.persistence.dao.EntityRemoved;
import io.onedev.server.security.ProjectPrivilege;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.util.BatchWorker;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.reviewrequirement.InvalidReviewRuleException;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.utils.concurrent.Prioritized;

@Singleton
public class DefaultPullRequestManager extends AbstractEntityManager<PullRequest> implements PullRequestManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestManager.class);
	
	private static final int MAX_CONTRIBUTION_FILES = 100;
	
	private static final int UI_PREVIEW_PRIORITY = 10;
	
	private static final int BACKEND_PREVIEW_PRIORITY = 50;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final UserManager userManager;
	
	private final UnitOfWork unitOfWork;
	
	private final ListenerRegistry listenerRegistry;
	
	private final PullRequestReviewManager pullRequestReviewManager;
	
	private final PullRequestBuildManager pullRequestBuildManager;

	private final BatchWorkManager batchWorkManager;
	
	private final PullRequestActionManager pullRequestActionManager;
	
	private final ConfigurationManager configurationManager;
	
	private final BuildManager buildManager;
	
	@Inject
	public DefaultPullRequestManager(Dao dao, PullRequestUpdateManager pullRequestUpdateManager,  
			PullRequestReviewManager pullRequestReviewManager, UserManager userManager, 
			MarkdownManager markdownManager, BatchWorkManager batchWorkManager, 
			ListenerRegistry listenerRegistry, UnitOfWork unitOfWork, 
			PullRequestActionManager pullRequestActionManager, BuildManager buildManager,
			ConfigurationManager configurationManager, PullRequestBuildManager pullRequestBuildManager) {
		super(dao);
		
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.pullRequestReviewManager = pullRequestReviewManager;
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.batchWorkManager = batchWorkManager;
		this.unitOfWork = unitOfWork;
		this.listenerRegistry = listenerRegistry;
		this.pullRequestActionManager = pullRequestActionManager;
		this.configurationManager = configurationManager;
		this.pullRequestBuildManager = pullRequestBuildManager;
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
			
			PullRequestAction action = new PullRequestAction();
			action.setDate(new Date());
			action.setData(new RestoredSourceBranchData(note));
			action.setRequest(request);
			action.setUser(userManager.getCurrent());
			pullRequestActionManager.save(action);
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null); 
		
		if (request.getSource().getObjectName(false) != null) {
			request.getSource().delete();
			
			PullRequestAction action = new PullRequestAction();
			action.setDate(new Date());
			action.setData(new DeletedSourceBranchData(note));
			action.setRequest(request);
			action.setUser(userManager.getCurrent());
			pullRequestActionManager.save(action);
		}
	}
	
	@Transactional
	@Override
	public void reopen(PullRequest request, String note) {
		User user = userManager.getCurrent();
		request.setCloseInfo(null);

		PullRequestAction action = new PullRequestAction();
		action.setDate(new Date());
		action.setData(new ReopenedData(note));
		action.setRequest(request);
		action.setUser(user);
		pullRequestActionManager.save(action);
		checkAsync(request);
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
		
		PullRequestAction action = new PullRequestAction();
		action.setDate(date);
		action.setData(new DiscardedData(note));
		action.setRequest(request);
		action.setUser(user);
		pullRequestActionManager.save(action);
	}
	
	private void merge(PullRequest request) {
		MergePreview preview = Preconditions.checkNotNull(request.getMergePreview());
		String merged = Preconditions.checkNotNull(preview.getMerged());
		
		ObjectId mergedId = ObjectId.fromString(merged);
		RevCommit mergedCommit = request.getTargetProject().getRevCommit(mergedId);
		
        PersonIdent committer = new PersonIdent(OneDev.NAME, "");
        
		Project targetProject = request.getTargetProject();
		MergeStrategy strategy = request.getMergeStrategy();
		if ((strategy == ALWAYS_MERGE || strategy == MERGE_IF_NECESSARY || strategy == SQUASH_MERGE) 
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
				Throwables.propagate(e);
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
		Subject subject = SecurityUtils.getSubject();
		ObjectId newTargetHeadId = mergedId;
		dao.doUnitOfWorkAsyncAfterCommit(new Runnable() {

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
	
	@Transactional
	@Override
	public void open(PullRequest request) {
		request.setNumber(getNextNumber(request.getTargetProject()));

		LastActivity lastActivity = new LastActivity();
		lastActivity.setDescription("submitted");
		lastActivity.setUser(request.getSubmitter());
		lastActivity.setDate(request.getSubmitDate());
		request.setLastActivity(lastActivity);
		
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
		pullRequestBuildManager.saveBuilds(request);

		checkAsync(request);
		
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
		
		PullRequestAction action = new PullRequestAction();
		action.setDate(date);
		action.setData(new MergedData(reason));
		action.setRequest(request);
		pullRequestActionManager.save(action);
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
					pullRequestBuildManager.saveBuilds(request);
					
					boolean allReviewsApproved = true;
					for (PullRequestReview review: request.getReviews()) {
						if (review.getExcludeDate() == null && 
								(review.getResult() == null || !review.getResult().isApproved())) {
							allReviewsApproved = false;
							break;
						}
					}
					boolean allBuildsSuccessful = true;
					for (PullRequestBuild build: request.getBuilds()) {
						if (build.getBuild() == null || build.getBuild().getStatus() != Build.Status.SUCCESS) {
							allBuildsSuccessful = false;
							break;
						}
					}
					
					MergePreview mergePreview = request.getMergePreview();
					
					if (allReviewsApproved && allBuildsSuccessful
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
			if (request.isOpen() && !request.isMergeIntoTarget()) {
				if (lastPreview == null || !lastPreview.isUpToDate(request)) {
					int priority = RequestCycle.get() != null?UI_PREVIEW_PRIORITY:BACKEND_PREVIEW_PRIORITY;			
					Long requestId = request.getId();
					dao.doAfterCommit(new Runnable() {
	
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
				unitOfWork.run(new Runnable() {

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
	public int countOpen(Project targetProject) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(PullRequest.CriterionHelper.ofOpen());
		criteria.add(PullRequest.CriterionHelper.ofTargetProject(targetProject));
		return count(criteria);
	}

	@Sessional
	@Override
	public PullRequest find(String uuid) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq("uuid", uuid));
		return find(criteria);
	}

	@Transactional
	@Listen
	public void on(PullRequestActionEvent event) {
		event.getRequest().setLastActivity(event);
		ActionData data = event.getAction().getData();
		if (data instanceof ApprovedData || data instanceof DiscardedData  
				|| data instanceof RemovedReviewerData || data instanceof AddedReviewerData 
				|| data instanceof ChangedMergeStrategyData) {
			checkAsync(event.getRequest());
		}
	}
	
	@Listen
	@Transactional
	public void on(BuildEvent event) {
		Build build = event.getBuild();
		for (PullRequest request: findOpenByCommit(build.getCommit())) { 
			listenerRegistry.post(new PullRequestBuildEvent(request, build));
			if (build.getStatus() != Build.Status.RUNNING)
				checkAsync(request);
		}
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
		dao.doUnitOfWorkAsyncAfterCommit(newCheckStatusRunnable(requestId, subject));
	}
	
	@Transactional
	@Override
	public void checkQuality(PullRequest request) {
		BranchProtection branchProtection = request.getTargetProject().getBranchProtection(request.getTargetBranch(), request.getSubmitter());
		if (branchProtection != null) {
			ReviewRequirement reviewRequirement = branchProtection.getReviewRequirement();
			if (reviewRequirement != null) 
				checkReviews(reviewRequirement, request.getLatestUpdate());

			if (branchProtection.getConfigurations() != null)
				checkBuilds(request, branchProtection.getConfigurations(), branchProtection.isBuildMerges());
			
			Set<FileProtection> checkedFileProtections = new HashSet<>();
			for (int i=request.getSortedUpdates().size()-1; i>=0; i--) {
				if (checkedFileProtections.containsAll(branchProtection.getFileProtections()))
					break;
				PullRequestUpdate update = request.getSortedUpdates().get(i);
				for (String file: update.getChangedFiles()) {
					FileProtection fileProtection = branchProtection.getFileProtection(file);
					if (fileProtection != null && !checkedFileProtections.contains(fileProtection)) {
						checkedFileProtections.add(fileProtection);
						checkReviews(fileProtection.getReviewRequirement(), update);
					}
				}
			}
		}
		
		ProjectFacade project = request.getTargetProject().getFacade();
		Permission writePermission = new ProjectPermission(project, ProjectPrivilege.WRITE); 
		if (request.getSubmitter() == null || !request.getSubmitter().asSubject().isPermitted(writePermission)) {
			Collection<User> writers = new ArrayList<>();
			for (UserFacade facade: SecurityUtils.getAuthorizedUsers(project, ProjectPrivilege.WRITE)) 
				writers.add(userManager.load(facade.getId()));
			checkReviews(writers, 1, request.getLatestUpdate());
		}
	}

	private void checkBuilds(PullRequest request, List<String> configurationNames, boolean buildMerges) {
		String commit;
		if (buildMerges) {
			MergePreview preview = request.getMergePreview();
			if (preview != null && preview.getMerged() != null) 
				commit = preview.getMerged();
			else 
				commit = null;
		} else {
			commit = request.getHeadCommitHash();			
		}
		
		Map<Configuration, Build> builds = new HashMap<>();
		if (commit != null) {
			for (Build build: buildManager.findAll(request.getTargetProject(), commit)) 
				builds.put(build.getConfiguration(), build);
		}
		Collection<Configuration> configurations = new HashSet<>();
		for (String configurationName: configurationNames) {
			Configuration configuration = configurationManager.find(request.getTargetProject(), configurationName);
			if (configuration != null) {
				configurations.add(configuration);
				PullRequestBuild pullRequestBuild = request.getBuild(configuration);
				if (pullRequestBuild == null) {
					pullRequestBuild = new PullRequestBuild();
					pullRequestBuild.setConfiguration(configuration);
					pullRequestBuild.setRequest(request);
					request.getBuilds().add(pullRequestBuild);
				}
				pullRequestBuild.setBuild(builds.get(configuration));
			} else {
				String errorMessage = String.format("Unable to find configuration (project: %s, name: %s)", 
						request.getTargetProject().getName(), configurationName);
				throw new OneException(errorMessage);
			}
		}
		
		for (Iterator<PullRequestBuild> it = request.getBuilds().iterator(); it.hasNext();) {
			if (!configurations.contains(it.next().getConfiguration()))
				it.remove();
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
			throw new InvalidReviewRuleException(errorMessage);
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
	
	@Transactional
	@Override
	public Collection<PullRequest> findOpenByCommit(String commitHash) {
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		criteria.add(PullRequest.CriterionHelper.ofOpen());
		Criterion verifyCommitCriterion = Restrictions.or(
				Restrictions.eq("headCommitHash", commitHash), 
				Restrictions.eq("lastMergePreview.merged", commitHash));
		criteria.add(verifyCommitCriterion);
		return findAll(criteria);
	}
	
	private Predicate[] getPredicates(io.onedev.server.entityquery.EntityCriteria<PullRequest> criteria, 
			Project targetProject, QueryBuildContext<PullRequest> context) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(context.getBuilder().equal(context.getRoot().get("targetProject"), targetProject));
		if (criteria != null)
			predicates.add(criteria.getPredicate(targetProject, context));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<PullRequest> buildCriteriaQuery(Session session, Project targetProject, 
			EntityQuery<PullRequest> requestQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<PullRequest> query = builder.createQuery(PullRequest.class);
		Root<PullRequest> root = query.from(PullRequest.class);
		query.select(root).distinct(true);
		
		QueryBuildContext<PullRequest> context = new PullRequestQueryBuildContext(root, builder);
		query.where(getPredicates(requestQuery.getCriteria(), targetProject, context));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: requestQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(PullRequestQuery.getPath(root, PullRequestConstants.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(PullRequestQuery.getPath(root, PullRequestConstants.ORDER_FIELDS.get(sort.getField()))));
		}

		Path<String> idPath = root.get("id");
		if (orders.isEmpty())
			orders.add(builder.desc(idPath));
		query.orderBy(orders);
		
		return query;
	}

	@Sessional
	@Override
	public List<PullRequest> query(Project targetProject, EntityQuery<PullRequest> requestQuery, int firstResult, int maxResults) {
		CriteriaQuery<PullRequest> criteriaQuery = buildCriteriaQuery(getSession(), targetProject, requestQuery);
		Query<PullRequest> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}
	
	@Sessional
	@Override
	public int count(Project targetProject, io.onedev.server.entityquery.EntityCriteria<PullRequest> requestCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		QueryBuildContext<PullRequest> context = new PullRequestQueryBuildContext(root, builder);
		criteriaQuery.where(getPredicates(requestCriteria, targetProject, context));

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
				source.getProject().getRepository(), source.getObjectId(), 
				GitUtils.branch2ref(source.getBranch()));
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
		
		Long number = null;
		String numberStr = term;
		if (numberStr != null) {
			numberStr = numberStr.trim();
			if (numberStr.startsWith("#"))
				numberStr = numberStr.substring(1);
			if (StringUtils.isNumeric(numberStr))
				number = Long.valueOf(numberStr);
		}
		
		if (number != null) {
			PullRequest request = find(targetProject, number);
			if (request != null)
				requests.add(request);
			EntityCriteria<PullRequest> criteria = newCriteria();
			criteria.add(Restrictions.eq("targetProject", targetProject));
			criteria.add(Restrictions.and(
					Restrictions.or(Restrictions.ilike("noSpaceTitle", "%" + term + "%"), Restrictions.ilike("numberStr", term + "%")), 
					Restrictions.ne("number", number)
				));
			criteria.addOrder(Order.desc("number"));
			requests.addAll(findRange(criteria, 0, count-requests.size()));
		} else {
			EntityCriteria<PullRequest> criteria = newCriteria();
			criteria.add(Restrictions.eq("targetProject", targetProject));
			if (StringUtils.isNotBlank(term)) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("noSpaceTitle", "%" + term + "%"), 
						Restrictions.ilike("numberStr", (term.startsWith("#")? term.substring(1): term) + "%")));
			}
			criteria.addOrder(Order.desc("number"));
			requests.addAll(findRange(criteria, 0, count));
		} 
		return requests;
	}
	
}
