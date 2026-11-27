package io.onedev.server.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
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
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.onedev.server.model.support.CompareContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Session;
import io.onedev.server.persistence.dao.Criterion;
import io.onedev.server.persistence.dao.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.joda.time.DateTime;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.data.DataService;
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
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.exception.RedundantPullRequestException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectNumberCounter;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestAutoMergeChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeStrategyChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestSourceBranchDeleteData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestSourceBranchRestoreData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestTargetBranchChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestWorkInProgressChangeData;
import io.onedev.server.persistence.SequenceGenerator;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.PendingSuggestionApplyService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestChangeService;
import io.onedev.server.service.PullRequestLabelService;
import io.onedev.server.service.PullRequestReviewService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectPullRequestStatusStat;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.QueryUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.util.StatsGroup;
import io.onedev.server.workspace.WorkspaceService;
import io.onedev.server.xodus.PullRequestInfoService;

@Singleton
public class DefaultPullRequestService extends BaseEntityService<PullRequest>
		implements PullRequestService, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultPullRequestService.class);

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
	private WorkspaceService workspaceService;

	@Inject
	private PullRequestChangeService changeService;

	@Inject
	private SessionService sessionService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private PendingSuggestionApplyService pendingSuggestionApplyService;

	@Inject
	private GitService gitService;

	@Inject
	private PullRequestInfoService pullRequestInfoService;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private DataService dataService;

	private SequenceGenerator numberGenerator;

	private synchronized SequenceGenerator getNumberGenerator() {
		if (numberGenerator == null)
			numberGenerator = new SequenceGenerator(PullRequest.class, ProjectNumberCounter.PROP_NEXT_PULL_REQUEST_NUMBER, clusterService, dao, dataService);
		return numberGenerator;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PullRequestService.class);
	}

	@Transactional
	@Override
	public void delete(PullRequest request) {
		request.lockForUpdate(getSession());
		checkNoWorkspaces(request);
		doDelete(request);
		listenerRegistry.post(new PullRequestDeleted(request));
	}

	private void checkNoWorkspaces(PullRequest request) {
		if (request.getWorkspaces().size() > 0) {
			throw new NotAcceptableException("Cannot delete pull request \""
					+ request.getReference().toString(request.getProject()) + "\" as it has workspaces");
		}
	}

	private void doDelete(PullRequest request) {
		Collection<String> refs = Lists.newArrayList(
				request.getHeadRef(), request.getMergeRef(), request.getBuildRef());
		for (PullRequestUpdate update: request.getUpdates())
			refs.add(update.getHeadRef());

		gitService.deleteRefs(request.getTargetProject(), refs);

		var query = getSession().createMutationQuery(String.format(
				"update CodeComment set %s=null where %s=:request",
				CodeComment.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST,
				CodeComment.PROP_COMPARE_CONTEXT + "." + CompareContext.PROP_PULL_REQUEST));
		query.setParameter("request", request);
		query.executeUpdate();

		dao.remove(request);
	}

	@Transactional
	@Override
	public void restoreSourceBranch(User user, PullRequest request, String note) {
        var errorMessage = request.checkRestoreSourceBranchCondition();
        if (errorMessage != null)
            throw new NotAcceptableException(errorMessage);

		if (request.getSource().getObjectName(false) == null) {
			gitService.createBranch(request.getSourceProject(), request.getSourceBranch(),
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
        var errorMessage = request.checkDeleteSourceBranchCondition();
        if (errorMessage != null)
            throw new NotAcceptableException(errorMessage);

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
		request.lockForUpdate(getSession());
		var errorMessage = request.checkReopenCondition();
        if (errorMessage != null)
            throw new NotAcceptableException(errorMessage);

		request.setStatus(OPEN);

		PullRequestChange change = new PullRequestChange();
		change.setData(new PullRequestReopenData());
		change.setRequest(request);
		change.setUser(user);
		changeService.create(change, note);

		request.setMergePreview(null);
		reconcileAsync(request);
	}

	@Transactional
	@Override
 	public void discard(User user, PullRequest request, String note) {
		request.lockForUpdate(getSession());
		discardLocked(user, request, note);
	}

	private void discardLocked(User user, PullRequest request, String note) {
        if (!request.isOpen())
            throw new NotAcceptableException("Pull request already closed");

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
		request.lockForUpdate(getSession());
		mergeLocked(user, request, commitMessage);
	}

	private void mergeLocked(User user, PullRequest request, @Nullable String commitMessage) {
        var errorMessage = request.checkMergeCondition();
        if (errorMessage != null)
            throw new NotAcceptableException(errorMessage);
        errorMessage = request.checkMergeCommitMessage(user, commitMessage);
        if (errorMessage != null)
            throw new NotAcceptableException("Error checking merge commit message: " + errorMessage);

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
			mergeCommitId = gitService.amendCommit(project, mergeCommitId,
					author, person, commitMessage);
		} else if (mergeStrategy == REBASE_SOURCE_BRANCH_COMMITS) {
			ObjectId targetHeadCommitId = ObjectId.fromString(mergePreview.getTargetHeadCommitHash());
			mergeCommitId = gitService.amendCommits(project, mergeCommitId, targetHeadCommitId,
					User.SYSTEM_NAME, person);
		}

		if (!mergeCommitId.name().equals(mergePreview.getMergeCommitHash())) {
			MergePreview lastMergePreview = mergePreview;
	        mergePreview = new MergePreview();
			mergePreview.setTargetBranch(lastMergePreview.getTargetBranch());
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

		if (project.findDeleteBranchAfterPullRequestMerge()) {
			if (!SecurityUtils.canDeleteBranch(user.asSubject(), request.getSourceProject(), request.getSourceBranch())) {
				logger.warn("User {} does not have permission to delete source branch after pull request merge", user.getName());
				return;
			}
			errorMessage = request.checkDeleteSourceBranchCondition();
			if (errorMessage != null) {
				logger.warn("Unable to delete source branch after pull request merge: {}", errorMessage);
				return;
			}
			if (workspaceService.count(request.getSourceProject(), request.getSourceBranch()) > 0) {
				logger.warn("Cannot delete source branch after pull request merge as it has workspaces");
				return;
			}
			projectService.deleteBranch(request.getSourceProject(), request.getSourceBranch());
		}
	}

	@Override
	@Sessional
	public void prepareDraft(PullRequest request) {
		Preconditions.checkArgument(request.isNew());
		var target = request.getTarget();
		var source = checkNotNull(request.getSource());
		checkNotNull(request.getSubmitter());

		if (target.equals(source))
			throw new RedundantPullRequestException("Source branch and target branch are the same");
		var existing = findEffective(target, source);
		if (existing != null) {
			throw new RedundantPullRequestException(existing.isOpen()
					? "Another pull request already opened for this change"
					: "Another pull request already merged the change");
		}

		var sourceHead = source.getObjectId();
		if (!request.isDraftPrepared()) {
			request.setUpdates(new ArrayList<>());
			addUpdate(request, sourceHead);
		}
		var mergeBase = request.getMergeBase(false);
		if (mergeBase == null)
			throw new NotAcceptableException("History of target branch and source branch are unrelated");
		if (mergeBase.equals(request.getLatestUpdate().getHeadCommitId()))
			throw new RedundantPullRequestException("Target branch is already up to date with source branch");

		if (request.isDraftPrepared())
			return;
		if (request.getAssignments().isEmpty()) {
			for (var assignee: target.getProject().findDefaultPullRequestAssignees()) {
				PullRequestAssignment assignment = new PullRequestAssignment();
				assignment.setRequest(request);
				assignment.setUser(assignee);
				request.getAssignments().add(assignment);
			}
		}

		if (request.getMergeStrategy() == null)
			request.setMergeStrategy(target.getProject().findDefaultPullRequestMergeStrategy());
		reviewService.reconcile(request.getSubmitter(), request, Set.of());
		request.setDraftPrepared(true);
	}

	private PullRequestUpdate addUpdate(PullRequest request, ObjectId head) {
		var update = new PullRequestUpdate();
		update.setRequest(request);
		update.setHeadCommitHash(head.name());
		request.getUpdates().add(update);
		return update;
	}

	@Transactional
	@Override
	public void open(PullRequest request) {
		prepareDraft(request);

		request.setNumberScope(request.getTargetProject().getForkRoot());
		request.setNumber(getNumberGenerator().getNextSequence(request.getNumberScope()));
		request.setSubmitDate(new Date());
		var initialUpdate = request.getLatestUpdate();
		initialUpdate.setDate(new DateTime().plusMillis(1).toDate());

		LastActivity lastActivity = new LastActivity();
		lastActivity.setUser(request.getSubmitter());
		lastActivity.setDescription("opened");
		lastActivity.setDate(request.getSubmitDate());
		request.setLastActivity(lastActivity);

		request.setMergePreview(null);
		dao.persist(request);

		dao.persist(initialUpdate);
		syncUpdateRefs(request, initialUpdate);

		for (PullRequestReview review: request.getReviews())
			dao.persist(review);

		for (PullRequestAssignment assignment: request.getAssignments())
			dao.persist(assignment);

		for (PullRequestLabel label: request.getLabels())
			dao.persist(label);

		reconcileAsync(request);

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

	private void syncUpdateRefs(PullRequest request, PullRequestUpdate update) {
		var head = update.getHeadCommitId();
		gitService.updateRef(request.getTargetProject(), update.getHeadRef(), head, null);
		gitService.updateRef(request.getTargetProject(), request.getHeadRef(), head, null);
	}

	private void syncRef(Project project, String refName, @Nullable ObjectId desired) {
		var current = gitService.resolve(project, refName, false);
		if (!Objects.equals(current, desired)) {
			if (desired != null)
				gitService.updateRef(project, refName, desired, null);
			else
				gitService.deleteRefs(project, Set.of(refName));
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

	void reconcilePreview(PullRequest request, boolean refreshBuildCommit) {
		var previousPreview = request.getMergePreview();
		var preview = request.checkMergePreview();
		boolean previewRecalculated = preview == null;
		if (previewRecalculated) {
			preview = MergePreview.calculate(request, "Merge preview of pull request #" + request.getNumber());
			request.setMergePreview(preview);
		}
		var previousBuildCommitHash = request.getBuildCommitHash();
		var mergeCommitHash = preview.getMergeCommitHash();
		if (!Objects.equals(previousBuildCommitHash, mergeCommitHash)) {
			boolean updateBuildCommit = refreshBuildCommit;
			if (!updateBuildCommit && previewRecalculated) {
				boolean canReuseBuildCommit = previousPreview != null
						&& previousBuildCommitHash != null
						&& Objects.equals(previousPreview.getTargetBranch(), preview.getTargetBranch())
						&& previousPreview.getHeadCommitHash().equals(preview.getHeadCommitHash())
						&& previousPreview.getMergeStrategy() == preview.getMergeStrategy();
				// Strict builds also follow changes to the target branch tip.
				updateBuildCommit = !canReuseBuildCommit || request.getBuildRequirement().isStrictMode();
			}
			if (updateBuildCommit) {
				request.setBuildCommitHash(mergeCommitHash);
				listenerRegistry.post(new PullRequestBuildCommitUpdated(request));
			}
		}
		if (previewRecalculated)
			listenerRegistry.post(new PullRequestMergePreviewUpdated(request));
		syncRef(request.getProject(), request.getMergeRef(), preview.getMergeCommitId());
		var buildHash = request.getBuildCommitHash();
		syncRef(request.getProject(), request.getBuildRef(), buildHash != null ? ObjectId.fromString(buildHash) : null);
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
			if (project.getForkRoot().equals(project))
				transactionService.runAfterCommit(() -> getNumberGenerator().removeNextSequence(project));
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
			for (PullRequest request: query(EntityCriteria.of(PullRequest.class).add(criterion)))
				reconcileAsync(request);
		}
	}

	@Sessional
	@Override
	public PullRequest findEffective(ProjectAndBranch target, ProjectAndBranch source) {
		var sourceHead = source.getObjectName(false);
		if (sourceHead == null)
			return findOpen(target, source);
		EntityCriteria<PullRequest> criteria = EntityCriteria.of(PullRequest.class);
		Criterion merged = Restrictions.and(
				Restrictions.eq(PROP_STATUS, MERGED),
				Restrictions.eq(PullRequest.PROP_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH, sourceHead));

		criteria.add(ofTarget(target)).add(ofSource(source)).add(Restrictions.or(ofOpen(), merged));
		// Prefer an open request if both an open and a merged request match.
		criteria.addOrder(io.onedev.server.persistence.dao.Order.asc(PROP_STATUS));

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
		if (event instanceof PullRequestOpened || event instanceof PullRequestBuildCommitUpdated
				|| event instanceof PullRequestMergePreviewUpdated || event instanceof PullRequestUpdated)
			gitDirModified(event.getProject().getId());
		if (event instanceof PullRequestChanged changed) {
			var data = changed.getChange().getData();
			if (data instanceof PullRequestApproveData || data instanceof PullRequestRequestedForChangesData
					|| data instanceof PullRequestWorkInProgressChangeData || data instanceof PullRequestAutoMergeChangeData
					|| data instanceof PullRequestMergeStrategyChangeData || data instanceof PullRequestTargetBranchChangeData)
				reconcileAsync(event.getRequest());
		} else if (event instanceof PullRequestReviewerRemoved
				|| event instanceof PullRequestBuildEvent buildEvent && buildEvent.getBuild().isSuccessful()) {
			reconcileAsync(event.getRequest());
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
		if (!event.isMinor() && build.getRequest() != null && build.getRequest().isOpen()) {
			if (build.getCommitHash().equals(build.getRequest().getBuildCommitHash()))
				listenerRegistry.post(new PullRequestBuildEvent(build));
		}
	}

	@Sessional
	@Override
	public void reconcileAsync(PullRequest pullRequest, boolean refreshBuildCommit) {
		var projectId = pullRequest.getTargetProject().getId();
		var requestId = pullRequest.getId();
		var sourceProject = pullRequest.getSourceProject();
		boolean fromFork = sourceProject != null && !sourceProject.equals(pullRequest.getTargetProject());
		// Wait for other PR events before checking out a connection.
		ClusterTask<Void> reconciliation = () -> LockUtils.call(getSerialLockName(requestId), true, () -> {
			try {
				record ForkUpdate(Long sourceId, String sourceRef, Long targetId, String targetRef, ObjectId head) {}

				var forkUpdate = fromFork ? sessionService.call(() -> {
					var request = get(requestId);
					if (request == null || !request.isOpen() || request.getSourceProject() == null)
						return null;
					var head = request.getSource().getObjectId(false);
					if (head == null)
						return null;
					var initialPreview = request.getMergePreview() == null;
					if (!initialPreview && head.equals(request.getLatestUpdate().getHeadCommitId()))
						return null;
					return new ForkUpdate(
							request.getSourceProject().getId(), request.getSourceRef(),
							request.getTargetProject().getId(), initialPreview ? request.getTargetRef() : request.getHeadRef(),
							head);
				}) : null;
				ObjectId copiedHead;
				if (forkUpdate != null) {
					// The Git/LFS HTTP handler can need a connection of its own.
					gitService.copyCommit(forkUpdate.sourceId(), forkUpdate.sourceRef(), forkUpdate.targetId(),
							forkUpdate.targetRef(), forkUpdate.head());
					copiedHead = forkUpdate.head();
				} else {
					copiedHead = null;
				}

				boolean autoMerge = transactionService.call(() -> {
					var request = get(requestId);
					if (request == null)
						return false;
					request.lockForUpdate(getSession());
					if (!request.isOpen())
						return false;
					var systemUser = userService.getSystem();
					if (request.getSourceProject() == null) {
						discardLocked(systemUser, request, "Source project no longer exists");
						return false;
					}
					var sourceHead = request.getSource().getObjectId(false);
					if (sourceHead == null || request.getTarget().getObjectId(false) == null) {
						discardLocked(systemUser, request, "Source or target branch no longer exists");
						return false;
					}

					var latest = request.getLatestUpdate();
					boolean sourceChanged = !sourceHead.equals(latest.getHeadCommitId());
					if (!request.getSourceProject().equals(request.getTargetProject())
							&& (sourceChanged || request.getMergePreview() == null)
							&& !sourceHead.equals(copiedHead)) {
						// On opening, even the recorded head may still need its first LFS transfer.
						reconcileAsync(request, refreshBuildCommit);
						return false;
					}
					if (sourceChanged) {
						request.getAutoMerge().setEnabled(false);
						latest = addUpdate(request, sourceHead);
						dao.persist(latest);
					}
					if (request.isCommitsMissing() || request.getMergeBase(false) == null)
						throw new ExplicitException("Pull request commits are missing or unrelated");
					if (request.isMergedIntoTarget()) {
						closeAsMerged(systemUser, request, true);
					} else {
						reviewService.reconcile(systemUser, request, Set.of());
						reconcilePreview(request, refreshBuildCommit);
					}
					if (sourceChanged) {
						syncUpdateRefs(request, latest);
						listenerRegistry.post(new PullRequestUpdated(latest));
					}
					return request.isOpen() && request.getAutoMerge().isEnabled();
				});
				if (autoMerge) {
					transactionService.run(() -> {
						var request = get(requestId);
						if (request != null) {
							request.lockForUpdate(getSession());
							if (request.getAutoMerge().isEnabled() && request.checkMergeCondition() == null)
								mergeLocked(userService.getSystem(), request, request.getAutoMerge().getCommitMessage());
						}
					});
				}
			} catch (Exception e) {
				logger.error("Error reconciling pull request " + requestId, e);
				transactionService.run(() -> {
					var request = get(requestId);
					if (request != null) {
						request.lockForUpdate(getSession());
						if (request.isOpen()) {
							var previousError = request.getCheckError();
							request.setCheckError("Error reconciling pull request: " + e);
							if (previousError == null)
								listenerRegistry.post(new PullRequestCheckFailed(request));
						}
					}
				});
			}
			return null;
		});
		transactionService.runAfterCommit(() -> projectService.submitToActiveServer(projectId, reconciliation));
	}

	private Predicate[] getPredicates(Subject subject, Project targetProject, @Nullable Criteria<PullRequest> criteria,
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
			return builder.asc(QueryUtils.getPath(root, PullRequest.SORT_FIELDS.get(sort.getField()).getProperty()));
		else
			return builder.desc(QueryUtils.getPath(root, PullRequest.SORT_FIELDS.get(sort.getField()).getProperty()));
	}

	@SuppressWarnings("rawtypes")
	private CriteriaQuery<PullRequest> buildCriteriaQuery(Subject subject, Session session, 
			@Nullable Project targetProject, EntityQuery<PullRequest> query) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<PullRequest> criteriaQuery = builder.createQuery(PullRequest.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		criteriaQuery.where(getPredicates(subject, targetProject, query.getCriteria(), criteriaQuery, root, builder));

		List<Order> orders = new ArrayList<>();
		for (EntitySort sort: query.getSorts()) 
			orders.add(getOrder(sort, builder, root));

		if (query.getCriteria() != null)
			orders.addAll(query.getCriteria().getPreferOrders(builder, root));

		for (EntitySort sort: query.getBaseSorts()) 
			orders.add(getOrder(sort, builder, root));

		var found = false;
		for (var order: orders) {
			if (order.getExpression() instanceof SqmPath) {
				var expr = (SqmPath) order.getExpression();
				if (expr.getReferencedPathSource().getPathName().equals(LastActivity.PROP_DATE)
						&& expr.getLhs() instanceof SqmPath
						&& ((SqmPath) expr.getLhs()).getReferencedPathSource().getPathName().equals(PullRequest.PROP_LAST_ACTIVITY)) {
					found = true;
					break;
				}
			}
		}
		if (!found)
			orders.add(builder.desc(QueryUtils.getPath(root, PullRequest.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE)));
		
		criteriaQuery.orderBy(orders);

		return criteriaQuery;
	}

	@Sessional
	@Override
	public List<PullRequest> query(Subject subject, Project targetProject, 
			EntityQuery<PullRequest> query, boolean loadExtraInfo, 
			int firstResult, int maxResults) {
		CriteriaQuery<PullRequest> criteriaQuery = buildCriteriaQuery(subject, getSession(), targetProject, query);
		Query<PullRequest> hibernateQuery = getSession().createQuery(criteriaQuery);
		hibernateQuery.setFirstResult(firstResult);
		hibernateQuery.setMaxResults(maxResults);

		List<PullRequest> requests = hibernateQuery.getResultList();
		if (!requests.isEmpty() && loadExtraInfo) {
			reviewService.populateReviews(requests);
			buildService.populateBuilds(requests);
			labelService.populateLabels(requests);
		}

		return requests;
	}

	@Sessional
	@Override
	public int count(Subject subject, Project targetProject,  Criteria<PullRequest> criteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		criteriaQuery.where(getPredicates(subject, targetProject, criteria, criteriaQuery, root, builder));

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
		var sorted = requests.stream().sorted().toList();
		for (var request : sorted) {
			request.lockForUpdate(getSession());
			checkNoWorkspaces(request);
		}
		for (var request : sorted)
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
			criteriaQuery.select(builder.construct(ProjectPullRequestStatusStat.class,
					root.get(PullRequest.PROP_TARGET_PROJECT).get(Project.PROP_ID),
					root.get(PROP_STATUS), builder.count(root)));
			criteriaQuery.groupBy(root.get(PullRequest.PROP_TARGET_PROJECT), root.get(PROP_STATUS));

			criteriaQuery.where(root.get(PullRequest.PROP_TARGET_PROJECT).in(projects));
			criteriaQuery.orderBy(builder.asc(root.get(PROP_STATUS)));
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	@Sessional
	@Override
	public Map<Integer, Integer> queryDurationStats(Subject subject, Project project, 
			Criteria<PullRequest> criteria, Date startDate, Date endDate, 
			StatsGroup statsGroup) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		var predicates = new ArrayList<Predicate>(asList(getPredicates(subject, project, criteria, criteriaQuery, root, builder)));
		predicates.add(builder.equal(root.get(PROP_STATUS), MERGED));
		predicates.add(builder.isNotNull(root.get(PROP_DURATION)));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_CLOSE_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_CLOSE_DATE), endDate));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		var groupPath = statsGroup.getPath(root.get(PROP_CLOSE_TIME_GROUPS));
		criteriaQuery.groupBy(groupPath);

		criteriaQuery.select(builder.array(
				groupPath,
				builder.avg(root.get(PROP_DURATION))));

		Map<Integer, Integer> stats = new HashMap<>();
		for (var result: getSession().createQuery(criteriaQuery).getResultList()) {
			var duration = ((Double)result[1])/1000;
			stats.put((int)result[0], (int)duration);
		}
		return stats;
	}

	@Sessional
	@Override
	public Map<Integer, Pair<Integer, Integer>> queryFrequencyStats(Subject subject, 
			Project project, Criteria<PullRequest> criteria, Date startDate, 
			Date endDate, StatsGroup statsGroup) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<PullRequest> root = criteriaQuery.from(PullRequest.class);

		var pullRequestPredicates = asList(getPredicates(subject, project, criteria, criteriaQuery, root, builder));

		var predicates = new ArrayList<>(pullRequestPredicates);
		predicates.add(builder.equal(root.get(PROP_STATUS), OPEN));
		if (startDate != null)
			predicates.add(builder.greaterThanOrEqualTo(root.get(PROP_SUBMIT_DATE), startDate));
		if (endDate != null)
			predicates.add(builder.lessThanOrEqualTo(root.get(PROP_SUBMIT_DATE), endDate));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		var groupPath = statsGroup.getPath(root.get(PROP_SUBMIT_TIME_GROUPS));
		criteriaQuery.groupBy(groupPath);

		criteriaQuery.select(builder.array(groupPath, builder.count(root)));

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

		criteriaQuery.select(builder.array(groupPath, builder.count(root)));

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

	@Sessional
	@Override
	public List<PullRequest> queryAfter(Long projectId, Long afterRequestId, int count) {
		EntityCriteria<PullRequest> criteria = newCriteria();
		criteria.add(io.onedev.server.persistence.dao.Restrictions.eq(PullRequest.PROP_TARGET_PROJECT + ".id", projectId));
		criteria.add(io.onedev.server.persistence.dao.Restrictions.gt("id", afterRequestId));
		criteria.addOrder(io.onedev.server.persistence.dao.Order.asc("id"));
		return query(criteria, 0, count);
	}

	@Override
	public Pair<String, String> suggestTitleAndDescription(PullRequest pullRequest, ChatModel chatModel, boolean suggestTitle, boolean suggestDescription) {
		if (!suggestTitle && !suggestDescription)
			return ImmutablePair.of(null, null);

		var sourceBranchSemantic = pullRequest.getSourceBranchSemantic();
		String titleSuggestInstruction;
		if (sourceBranchSemantic.getWorkType() != null) {
			titleSuggestInstruction = """
				When suggesting pull request title, you should not add work in progress prefix
				or conventional commit type prefix to the title even if commit messages
				indicate that.
				""";
		} else {
			titleSuggestInstruction = """
				When suggesting pull request title, you should not add work in progress prefix
				to the title even if commit messages indicate that.
				""";
		}

		var commitMessages = pullRequest.getLatestUpdate().getCommits().stream()
				.map(it -> it.getFullMessage())
				.collect(Collectors.toList());
		String userPromptText;
		try {
			userPromptText = "A json array of commit messages:\n" + objectMapper.writeValueAsString(commitMessages);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		var userMessage = new UserMessage(userPromptText);

		String title;
		String description;
		if (!suggestTitle) {
			var systemMessage = new SystemMessage(String.format("""
				You are a helpful assistant that can suggest pull request description by 
				summarizing multiple commit messages. Maximum %d characters allowed for 
				the description.
				
				IMPORTANT: only return the description, no other text or comments.
				""", PullRequest.MAX_DESCRIPTION_LEN));
			description = chatModel.chat(systemMessage, userMessage).aiMessage().text();
			title = null;
		} else if (!suggestDescription) {
			var systemMessage = new SystemMessage(String.format("""
				You are a helpful assistant that can suggest pull request title by summarizing 
				multiple commit messages. %s Maximum %d characters allowed for the title. 

				IMPORTANT: only return the title, no other text or comments.
				""", titleSuggestInstruction, PullRequest.MAX_TITLE_LEN));
			title = (pullRequest.getTitlePrefix(sourceBranchSemantic) + chatModel.chat(systemMessage, userMessage).aiMessage().text()).trim();
			description = null;
		} else {
			var systemMessage = new SystemMessage(String.format("""
				You are a helpful assistant that can suggest pull request title and description 
				by summarizing multiple commit messages. %s Maximum %d characters allowed for the title, 
				and %d characters allowed for the description.

				IMPORTANT: only return a VALID json object with "title" property set to the title and "description" 
				property set to the description, no other text or comments.
				""", titleSuggestInstruction, PullRequest.MAX_TITLE_LEN, PullRequest.MAX_DESCRIPTION_LEN));
			var responseText = chatModel.chat(systemMessage, userMessage).aiMessage().text();
			if (responseText.startsWith("```json"))
				responseText = responseText.substring("```json".length());
			if (responseText.endsWith("```"))
				responseText = responseText.substring(0, responseText.length() - "```".length());
			try {
				var response = objectMapper.readTree(responseText);
				if (response.has("title"))
					title = (pullRequest.getTitlePrefix(sourceBranchSemantic) + response.get("title").asText()).trim();
				else
					title = null;
				if (response.has("description"))
					description = response.get("description").asText();
				else
					description = null;
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return ImmutablePair.of(title, description);
	}

}
