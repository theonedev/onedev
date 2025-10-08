package io.onedev.server.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.model.PullRequest.PROP_CLOSE_DATE;
import static io.onedev.server.model.PullRequest.PROP_CLOSE_TIME_GROUPS;
import static io.onedev.server.model.PullRequest.PROP_DURATION;
import static io.onedev.server.model.PullRequest.PROP_STATUS;
import static io.onedev.server.model.PullRequest.PROP_SUBMIT_DATE;
import static io.onedev.server.model.PullRequest.PROP_SUBMIT_TIME_GROUPS;
import static io.onedev.server.model.PullRequest.getSerialLockName;
import static io.onedev.server.model.PullRequest.Status.MERGED;
import static io.onedev.server.model.PullRequest.Status.OPEN;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static java.util.Arrays.asList;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PendingSuggestionApplyService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestLabelService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestUpdateService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.project.pullrequest.PullRequestBuildCommitUpdated;
import io.onedev.server.event.project.pullrequest.PullRequestBuildEvent;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestCheckFailed;
import io.onedev.server.event.project.pullrequest.PullRequestCodeCommentEvent;
import io.onedev.server.event.project.pullrequest.PullRequestDeleted;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.project.pullrequest.PullRequestMergePreviewUpdated;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.event.project.pullrequest.PullRequestReviewerRemoved;
import io.onedev.server.event.project.pullrequest.PullRequestUpdated;
import io.onedev.server.event.project.pullrequest.PullRequestsDeleted;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
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
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.FileProtection;
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
import io.onedev.server.persistence.SequenceGenerator;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStatusStat;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.web.util.StatsGroup;
import io.onedev.server.xodus.CommitInfoService;
import io.onedev.server.xodus.PullRequestInfoService;

@Singleton
public class DefaultPullRequestService extends BaseEntityService<PullRequest>
		implements PullRequestService, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestService.class);

	@Inject
	private PullRequestUpdateService updateService;

	@Inject
	private ProjectService projectService;

	@Inject
	private UserService userService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Inject
	private PullRequestReviewService reviewService;

	@Inject
	private BuildService buildService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private PullRequestLabelService labelService;

	@Inject
	private CommitInfoService commitInfoService;

	@Inject
	private PullRequestChangeService changeService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private PendingSuggestionApplyService pendingSuggestionApplyService;

	@Inject
	private GitService gitService;

	@Inject
	private PullRequestInfoService pullRequestInfoService;

	private SequenceGenerator numberGenerator;

	private synchronized SequenceGenerator getNumberGenerator() {
		if (numberGenerator == null)
			numberGenerator = new SequenceGenerator(PullRequest.class, clusterService, dao);
		return numberGenerator;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestService.class);
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
	public void restoreSourceBranch(User user, PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null);
		if (request.getSource().getObjectName(false) == null) {
			getGitService().createBranch(request.getSourceProject(), request.getSourceBranch(),
					request.getLatestUpdate().getHeadCommitHash());

			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setData(new PullRequestSourceBranchRestoreData());
			change.setRequest(request);
			change.setUser(user);
			changeService.create(change, note);
		}
	}

	@Transactional
	@Override
	public void deleteSourceBranch(User user, PullRequest request, String note) {
		Preconditions.checkState(!request.isOpen() && request.getSourceProject() != null);

		if (request.getSource().getObjectName(false) != null) {
			projectService.deleteBranch(request.getSourceProject(), request.getSourceBranch());
			PullRequestChange change = new PullRequestChange();
			change.setDate(new Date());
			change.setData(new PullRequestSourceBranchDeleteData());
			change.setRequest(request);
			change.setUser(user);
			changeService.create(change, note);
		}
	}

	@Transactional
	@Override
	public void reopen(User user, PullRequest request, String note) {
		request.setStatus(OPEN);

		PullRequestChange change = new PullRequestChange();
		change.setData(new PullRequestReopenData());
		change.setRequest(request);
		change.setUser(user);
		changeService.create(change, note);

		MergePreview mergePreview = request.checkMergePreview();
		if (mergePreview != null)
			updateMergePreviewRef(request);

		checkAsync(request, false, true);
	}

	@Transactional
	@Override
 	public void discard(User user, PullRequest request, String note) {
		request.setStatus(Status.DISCARDED);
		request.setCloseDate(new Date());

		PullRequestChange change = new PullRequestChange();
		change.setData(new PullRequestDiscardData());
		change.setRequest(request);
		change.setUser(user);
		changeService.create(change, note);

		pendingSuggestionApplyService.discard(null, request);
	}

	@Transactional
	@Override
	public void merge(User user, PullRequest request, @Nullable String commitMessage) {
		MergePreview mergePreview = checkNotNull(request.checkMergePreview());
		ObjectId mergeCommitId = ObjectId.fromString(checkNotNull(mergePreview.getMergeCommitHash()));
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
			if (commitMessage == null)
				commitMessage = request.getDefaultMergeCommitMessage();
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

		closeAsMerged(user, request, false);

		gitService.updateRef(project, request.getTargetRef(), mergeCommitId,
				ObjectId.fromString(mergePreview.getTargetHeadCommitHash()));

		if (project.findDeleteBranchAfterPullRequestMerge()
				&& request.checkDeleteSourceBranchCondition() == null
				&& SecurityUtils.canDeleteBranch(user.asSubject(), request.getSourceProject(), request.getSourceBranch())) {
			gitService.deleteBranch(request.getSourceProject(), request.getSourceBranch());
		}
	}

	@Transactional
	@Override
	public void open(PullRequest request) {
		Preconditions.checkArgument(request.isNew());

		request.setNumberScope(request.getTargetProject().getForkRoot());
		request.setNumber(getNumberGenerator().getNextSequence(request.getNumberScope()));
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
			updateService.create(update);

		for (PullRequestReview review: request.getReviews())
			dao.persist(review);

		for (PullRequestAssignment assignment: request.getAssignments())
			dao.persist(assignment);

		for (PullRequestLabel label: request.getLabels())
			dao.persist(label);

		MergePreview mergePreview = request.checkMergePreview();

		if (mergePreview != null)
			updateMergePreviewRef(request);

		checkAsync(request, false, true);

		listenerRegistry.post(new PullRequestOpened(request));
	}

	private void closeAsMerged(User user, PullRequest request, boolean dueToMerged) {
		request.setStatus(MERGED);
		request.setCloseDate(new Date());

		Date date = new DateTime().plusMillis(1).toDate();

		if (dueToMerged)
			request.setMergePreview(null);

		String reason;
		if (dueToMerged)
			reason = "merged pull request";
		else
			reason = null;

		PullRequestChange change = new PullRequestChange();
		change.setUser(user);
		change.setDate(date);
		change.setData(new PullRequestMergeData(reason));
		change.setRequest(request);
		changeService.create(change, null);

		pendingSuggestionApplyService.discard(null, request);
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
		var gitDir = projectService.getRepository(projectId).getDirectory();
		projectService.directoryModified(projectId, gitDir);
	}

	@Listen
	public void on(PullRequestDeleted event) {
		gitDirModified(event.getProject().getId());
	}

	@Listen
	public void on(PullRequestsDeleted event) {
		gitDirModified(event.getProject().getId());
	}

	@Transactional
	@Override
	public void checkAutoMerge(PullRequest request) {
		if (request.isOpen()) {
			var autoMerge = request.getAutoMerge();
			if (autoMerge.isEnabled() && request.checkMergeCondition() == null) {
				merge(userService.getSystem(), request, autoMerge.getCommitMessage());
			}
		}
	}

	private void check(PullRequest request, boolean sourceUpdated, boolean updateBuildCommit) {
		try {
			request.setCheckError(null);
			if (request.isOpen() && request.isValid()) {
				var systemUser = userService.getSystem();
				if (request.getSourceProject() == null) {
					discard(systemUser, request, "Source project no longer exists");
				} else if (request.getSource().getObjectId(false) == null) {
					discard(systemUser, request, "Source branch no longer exists");
				} else if (request.getTarget().getObjectId(false) == null) {
					discard(systemUser, request, "Target branch no longer exists");
				} else {
					updateService.checkUpdate(request);
					if (request.isMergedIntoTarget()) {
						closeAsMerged(systemUser, request, true);
					} else {
						checkReviews(request, sourceUpdated);

						for (PullRequestReview review: request.getReviews()) {
							if (review.isNew() || review.isDirty())
								reviewService.createOrUpdate(systemUser, review);
						}

						Project targetProject = request.getTargetProject();
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
						checkAutoMerge(request);
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
	    		getNumberGenerator().removeNextSequence(project);
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
				Restrictions.eq(PROP_STATUS, MERGED),
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
					Restrictions.eq(PROP_STATUS, MERGED),
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
	public void on(PullRequestEvent event) {
		if (event instanceof PullRequestOpened
				|| event instanceof PullRequestBuildCommitUpdated) {
			gitDirModified(event.getProject().getId());
		} else if (event instanceof PullRequestMergePreviewUpdated) {
			gitDirModified(event.getProject().getId());
			checkAutoMerge(event.getRequest());
		} if (event instanceof PullRequestChanged) {
			PullRequestChangeData data = ((PullRequestChanged) event).getChange().getData();
			if (data instanceof PullRequestApproveData) {
				checkAutoMerge(event.getRequest());
			} else if (data instanceof PullRequestMergeStrategyChangeData
					|| data instanceof PullRequestTargetBranchChangeData) {
				check(event.getRequest(), false, true);
			}
		} else if (event instanceof PullRequestBuildEvent) {
			var build = ((PullRequestBuildEvent) event).getBuild();
			if (build.isSuccessful())
				checkAutoMerge(event.getRequest());
		} else if (event instanceof PullRequestReviewerRemoved) {
			checkAutoMerge(event.getRequest());
		} else if (event instanceof PullRequestUpdated) {
			gitDirModified(event.getProject().getId());
		}

		if (!(event instanceof PullRequestOpened) && !event.isMinor()) {
			event.getRequest().setLastActivity(event.getLastUpdate());
			if (event instanceof PullRequestCodeCommentEvent)
				event.getRequest().setCodeCommentsUpdateDate(event.getDate());
		}
	}

	@Listen
	@Sessional
	public void on(BuildEvent event) {
		Build build = event.getBuild();
		if (build.getRequest() != null && build.getRequest().isOpen()) {
			if (build.getCommitHash().equals(build.getRequest().getBuildCommitHash()))
				listenerRegistry.post(new PullRequestBuildEvent(build));
		}
	}

	@Sessional
	public void checkAsync(PullRequest request, boolean sourceUpdated, boolean updateBuildCommit) {
		Long projectId = request.getTargetProject().getId();
		Long requestId = request.getId();

		transactionService.runAfterCommit(new ClusterRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public void run() {
				projectService.submitToActiveServer(projectId, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() {
						try {
							LockUtils.call(getSerialLockName(requestId), true, new ClusterTask<Void>() {

								private static final long serialVersionUID = 1L;

								@Override
								public Void call() {
									transactionService.run(new ClusterRunnable() {

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
		if (request.isOpen()) {
			BranchProtection branchProtection = request.getTargetProject()
					.getBranchProtection(request.getTargetBranch(), request.getSubmitter());
			Collection<String> changedFiles;
			if (sourceUpdated) {
				changedFiles = request.getLatestUpdate().getChangedFiles();
			} else {
				changedFiles = new HashSet<>();
				for (PullRequestUpdate update : request.getUpdates())
					changedFiles.addAll(update.getChangedFiles());
			}
			checkReviews(branchProtection.getParsedReviewRequirement(),
					request, changedFiles, sourceUpdated);

			ReviewRequirement checkedRequirement = ReviewRequirement.parse(null);

			for (String file : changedFiles) {
				FileProtection fileProtection = branchProtection.getFileProtection(file);
				if (!checkedRequirement.covers(fileProtection.getParsedReviewRequirement())) {
					checkedRequirement.mergeWith(fileProtection.getParsedReviewRequirement());
					checkReviews(fileProtection.getParsedReviewRequirement(),
							request, Sets.newHashSet(file), sourceUpdated);
				}
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

			for (Long reviewerId: commitInfoService.sortUsersByContribution(
					candidateReviewerEmails, project.getId(), changedFiles)) {
				User user = userService.load(reviewerId);
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
				if (users.contains(review.getUser()) && review.getStatus() == PullRequestReview.Status.EXCLUDED) {
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

	private Predicate[] getPredicates(Subject subject, Project targetProject, Criteria<PullRequest> criteria,
			CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (targetProject != null) {
			predicates.add(builder.equal(from.get(PullRequest.PROP_TARGET_PROJECT), targetProject));
		} else if (!SecurityUtils.isAdministrator(subject)) {
			Collection<Project> projects = SecurityUtils.getAuthorizedProjects(subject, new ReadCode());
			if (!projects.isEmpty()) {
				Path<Long> projectIdPath = from.get(PullRequest.PROP_TARGET_PROJECT).get(Project.PROP_ID);
				predicates.add(Criteria.forManyValues(builder, projectIdPath,
						projects.stream().map(it->it.getId()).collect(Collectors.toSet()),
						projectService.getIds()));
			} else {
				predicates.add(builder.disjunction());
			}
		}

		if (criteria != null) {
			var projectScope = targetProject!=null?new ProjectScope(targetProject, false, false):null;
			predicates.add(criteria.getPredicate(projectScope, query, from, builder));
		}
		return predicates.toArray(new Predicate[0]);
	}

	private Order getOrder(EntitySort sort, CriteriaBuilder builder, From<PullRequest, PullRequest> root) {
		if (sort.getDirection() == Direction.ASCENDING)
			return builder.asc(PullRequestQuery.getPath(root, PullRequest.SORT_FIELDS.get(sort.getField()).getProperty()));
		else
			return builder.desc(PullRequestQuery.getPath(root, PullRequest.SORT_FIELDS.get(sort.getField()).getProperty()));
	}

	@SuppressWarnings("rawtypes")
	private CriteriaQuery<PullRequest> buildCriteriaQuery(Subject subject, Session session, 
			@Nullable Project targetProject, EntityQuery<PullRequest> requestQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<PullRequest> query = builder.createQuery(PullRequest.class);
		Root<PullRequest> root = query.from(PullRequest.class);

		query.where(getPredicates(subject, targetProject, requestQuery.getCriteria(), query, root, builder));

		List<Order> orders = new ArrayList<>();
		for (EntitySort sort: requestQuery.getSorts()) 
			orders.add(getOrder(sort, builder, root));

		if (requestQuery.getCriteria() != null)
			orders.addAll(requestQuery.getCriteria().getPreferOrders(builder, root));

		for (EntitySort sort: requestQuery.getBaseSorts()) 
			orders.add(getOrder(sort, builder, root));

		var found = false;
		for (var order: orders) {
			if (order.getExpression() instanceof SingularAttributePath) {
				var expr = (SingularAttributePath) order.getExpression();
				if (expr.getAttribute().getName().equals(LastActivity.PROP_DATE) 
						&& expr.getPathSource() instanceof SingularAttributePath 
						&& ((SingularAttributePath) expr.getPathSource()).getAttribute().getName().equals(PullRequest.PROP_LAST_ACTIVITY)) {
					found = true;
					break;
				}
			}
		}
		if (!found)
			orders.add(builder.desc(PullRequestQuery.getPath(root, PullRequest.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE)));
		
		query.orderBy(orders);

		return query;
	}

	@Sessional
	@Override
	public List<PullRequest> query(Subject subject, Project targetProject, 
			EntityQuery<PullRequest> requestQuery, boolean loadExtraInfo, 
			int firstResult, int maxResults) {
		CriteriaQuery<PullRequest> criteriaQuery = buildCriteriaQuery(subject, getSession(), targetProject, requestQuery);
		Query<PullRequest> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);

		List<PullRequest> requests = query.getResultList();
		if (!requests.isEmpty() && loadExtraInfo) {
			reviewService.populateReviews(requests);
			buildService.populateBuilds(requests);
			labelService.populateLabels(requests);
		}

		return requests;
	}

	@Sessional
	@Override
	public int count(Subject subject, Project targetProject,  Criteria<PullRequest> requestCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		criteriaQuery.where(getPredicates(subject, targetProject, requestCriteria, criteriaQuery, root, builder));

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
	public PullRequest find(String uuid) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(Restrictions.eq(PullRequest.PROP_UUID, uuid));
		criteria.setCacheable(true);
		return find(criteria);
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
	public List<ProjectPullRequestStatusStat> queryStatusStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectPullRequestStatusStat> criteriaQuery = builder.createQuery(ProjectPullRequestStatusStat.class);
			Root<PullRequest> root = criteriaQuery.from(PullRequest.class);
			criteriaQuery.multiselect(
					root.get(PullRequest.PROP_TARGET_PROJECT).get(Project.PROP_ID),
					root.get(PROP_STATUS), builder.count(root));
			criteriaQuery.groupBy(root.get(PullRequest.PROP_TARGET_PROJECT), root.get(PROP_STATUS));

			criteriaQuery.where(root.get(PullRequest.PROP_TARGET_PROJECT).in(projects));
			criteriaQuery.orderBy(builder.asc(root.get(PROP_STATUS)));
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	@Sessional
	@Override
	public Map<Integer, Integer> queryDurationStats(Subject subject, Project project, 
			Criteria<PullRequest> pullRequestCriteria, Date startDate, Date endDate, 
			StatsGroup statsGroup) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		var predicates = new ArrayList<Predicate>(asList(getPredicates(subject, project, pullRequestCriteria, criteriaQuery, root, builder)));
		predicates.add(builder.equal(root.get(PROP_STATUS), MERGED));
		predicates.add(builder.isNotNull(root.get(PROP_DURATION)));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_CLOSE_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_CLOSE_DATE), endDate));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		var groupPath = statsGroup.getPath(root.get(PROP_CLOSE_TIME_GROUPS));
		criteriaQuery.groupBy(groupPath);

		criteriaQuery.multiselect(newArrayList(
				groupPath,
				builder.avg(root.get(PROP_DURATION))));

		Map<Integer, Integer> stats = new HashMap<>();
		for (var result: getSession().createQuery(criteriaQuery).getResultList()) {
			var duration = ((Double)result[1])/60000;
			stats.put((int)result[0], (int)duration);
		}
		return stats;
	}

	@Sessional
	@Override
	public Map<Integer, Pair<Integer, Integer>> queryFrequencyStats(Subject subject, 
			Project project, Criteria<PullRequest> pullRequestCriteria, Date startDate, 
			Date endDate, StatsGroup statsGroup) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		var pullRequestPredicates = asList(getPredicates(subject, project, pullRequestCriteria, criteriaQuery, root, builder));

		var predicates = new ArrayList<>(pullRequestPredicates);
		predicates.add(builder.equal(root.get(PROP_STATUS), OPEN));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_SUBMIT_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_SUBMIT_DATE), endDate));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		var groupPath = statsGroup.getPath(root.get(PROP_SUBMIT_TIME_GROUPS));
		criteriaQuery.groupBy(groupPath);

		criteriaQuery.multiselect(newArrayList(groupPath, builder.count(root)));

		Map<Integer, Integer> openStats = new HashMap<>();
		for (var result: getSession().createQuery(criteriaQuery).getResultList())
			openStats.put((int)result[0], ((Long)result[1]).intValue());

		predicates = new ArrayList<>(pullRequestPredicates);
		predicates.add(builder.equal(root.get(PROP_STATUS), MERGED));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_CLOSE_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_CLOSE_DATE), endDate));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		groupPath = statsGroup.getPath(root.get(PROP_CLOSE_TIME_GROUPS));
		criteriaQuery.groupBy(groupPath);

		criteriaQuery.multiselect(newArrayList(groupPath, builder.count(root)));

		Map<Integer, Integer> mergeStats = new HashMap<>();
		for (var result: getSession().createQuery(criteriaQuery).getResultList())
			mergeStats.put((int)result[0], ((Long)result[1]).intValue());

		Map<Integer, Pair<Integer, Integer>> stats = new HashMap<>();
		for (var entry: openStats.entrySet())
			stats.put(entry.getKey(), new ImmutablePair<>(entry.getValue(), 0));
		for (var entry: mergeStats.entrySet()) {
			var value = stats.get(entry.getKey());
			if (value != null)
				stats.put(entry.getKey(), new ImmutablePair<>(value.getLeft(), entry.getValue()));
			else
				stats.put(entry.getKey(), new ImmutablePair<>(0, entry.getValue()));
		}
		return stats;
	}

	private Criterion ofOpen() {
		return Restrictions.eq(PROP_STATUS, OPEN);
	}

	private Criterion ofTarget(ProjectAndBranch target) {
		return Restrictions.and(
				Restrictions.eq(PullRequest.PROP_TARGET_PROJECT, target.getProject()),
				Restrictions.eq(PullRequest.PROP_TARGET_BRANCH, target.getBranch()));
	}

	private Criterion ofSource(ProjectAndBranch source) {
		return Restrictions.and(
				Restrictions.eq(PullRequest.PROP_SOURCE_PROJECT, source.getProject()),
				Restrictions.eq(PullRequest.PROP_SOURCE_BRANCH, source.getBranch()));
	}

	@Sessional
	@Override
	public ObjectId getComparisonBase(PullRequest request, ObjectId oldCommitId, ObjectId newCommitId) {
		if (request.isNew() || oldCommitId.equals(newCommitId))
			return oldCommitId;

		Project targetProject = request.getTargetProject();
		ObjectId comparisonBase = pullRequestInfoService.getComparisonBase(request, oldCommitId, newCommitId);
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
				pullRequestInfoService.cacheComparisonBase(request, oldCommitId, newCommitId, comparisonBase);
			else
				throw new IllegalStateException();
		}
		return comparisonBase;
	}

	@Sessional
	@Override
	public List<PullRequest> query(User submitter, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<PullRequest> query = builder.createQuery(PullRequest.class);
		From<PullRequest, PullRequest> root = query.from(PullRequest.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(PullRequest.PROP_SUBMITTER), submitter));
		predicates.add(builder.greaterThanOrEqualTo(root.get(PullRequest.PROP_SUBMIT_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(PullRequest.PROP_SUBMIT_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}

	@Sessional
	@Override
	public Collection<Long> getTargetProjectIds() {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);
		
		criteriaQuery.select(root.get(PullRequest.PROP_TARGET_PROJECT).get(Project.PROP_ID));
		criteriaQuery.distinct(true);
		
		return getSession().createQuery(criteriaQuery).getResultList();
	}

}
