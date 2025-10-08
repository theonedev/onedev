package io.onedev.server.service.impl;

import static java.lang.Integer.MAX_VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.Input;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.entityreference.ReferenceChangeService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.build.BuildFinished;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueDescriptionRevision;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueConfidentialChangeData;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.model.support.issue.changedata.IssueIterationAddData;
import io.onedev.server.model.support.issue.changedata.IssueIterationChangeData;
import io.onedev.server.model.support.issue.changedata.IssueIterationRemoveData;
import io.onedev.server.model.support.issue.changedata.IssueLinkAddData;
import io.onedev.server.model.support.issue.changedata.IssueLinkRemoveData;
import io.onedev.server.model.support.issue.changedata.IssueOwnEstimatedTimeChangeData;
import io.onedev.server.model.support.issue.changedata.IssueOwnSpentTimeChangeData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTitleChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTotalEstimatedTimeChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTotalSpentTimeChangeData;
import io.onedev.server.model.support.issue.transitionspec.BranchUpdatedSpec;
import io.onedev.server.model.support.issue.transitionspec.BuildSuccessfulSpec;
import io.onedev.server.model.support.issue.transitionspec.IssueStateTransitedSpec;
import io.onedev.server.model.support.issue.transitionspec.NoActivitySpec;
import io.onedev.server.model.support.issue.transitionspec.PullRequestDiscardedSpec;
import io.onedev.server.model.support.issue.transitionspec.PullRequestMergedSpec;
import io.onedev.server.model.support.issue.transitionspec.PullRequestOpenedSpec;
import io.onedev.server.model.support.issue.transitionspec.PullRequestSpec;
import io.onedev.server.model.support.issue.transitionspec.TransitionSpec;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.search.entity.issue.LastActivityDateCriteria;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueDescriptionRevisionService;
import io.onedev.server.service.IssueFieldService;
import io.onedev.server.service.IssueLinkService;
import io.onedev.server.service.IssueScheduleService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultIssueChangeService extends BaseEntityService<IssueChange>
		implements IssueChangeService, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueChangeService.class);
	
	private static final int NO_ACTIVITY_TRANSITION_PRIORITY = 40;

	private static final int MAX_FIXED_ISSUES = 1000;

	@Inject
	private IssueService issueService;

	@Inject
	private IssueFieldService issueFieldService;

	@Inject
	private IssueDescriptionRevisionService descriptionRevisionService;

	@Inject
	private ProjectService projectService;

	@Inject
	private IssueScheduleService issueScheduleService;

	@Inject
	private IssueLinkService issueLinkService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private ClusterService clusterService;

	@Inject
	private ReferenceChangeService referenceChangeService;

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;

	@Inject
	private TransactionService transactionService;
	
	private String taskId;

	@Transactional
	@Override
	public void create(IssueChange change, @Nullable String note) {
		create(change, note, !change.getUser().isServiceAccount());
	}

	@Transactional
	protected void create(IssueChange change, @Nullable String note, boolean sendNotifications) {
		Preconditions.checkState(change.isNew());
		change.getIssue().getChanges().add(change);
		dao.persist(change);
		if (note != null && change.getUser() != null) {
			IssueComment comment = new IssueComment();
			comment.setContent(note);
			comment.setUser(change.getUser());
			comment.setIssue(change.getIssue());
			comment.setDate(new DateTime(change.getDate()).minusSeconds(1).toDate());
			dao.persist(comment);
			comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()+1);
		}

		listenerRegistry.post(new IssueChanged(change, note, sendNotifications));
	}
	
	@Transactional
	@Override
	public void changeTitle(User user, Issue issue, String title) {
		String prevTitle = issue.getTitle();
		if (!title.equals(prevTitle)) {
			issue.setTitle(title);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueTitleChangeData(prevTitle, issue.getTitle()));
			create(change, null);
			dao.persist(issue);
		}
	}

	@Transactional
	@Override
	public void changeOwnEstimatedTime(User user, Issue issue, int ownEstimatedTime) {
		int prevOwnEstimatedTime = issue.getOwnEstimatedTime();
		if (ownEstimatedTime != prevOwnEstimatedTime) {
			issue.setOwnEstimatedTime(ownEstimatedTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueOwnEstimatedTimeChangeData(prevOwnEstimatedTime, issue.getOwnEstimatedTime()));
			create(change, null);
			dao.persist(issue);
		}
	}

	@Transactional
	@Override
	public void changeOwnSpentTime(User user, Issue issue, int ownSpentTime) {
		int prevOwnSpentTime = issue.getOwnSpentTime();
		if (ownSpentTime != prevOwnSpentTime) {
			issue.setOwnSpentTime(ownSpentTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueOwnSpentTimeChangeData(prevOwnSpentTime, issue.getOwnSpentTime()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void changeTotalEstimatedTime(User user, Issue issue, int totalEstimatedTime) {
		int prevTotalEstimatedTime = issue.getTotalEstimatedTime();
		if (totalEstimatedTime != prevTotalEstimatedTime) {
			issue.setTotalEstimatedTime(totalEstimatedTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueTotalEstimatedTimeChangeData(prevTotalEstimatedTime, issue.getTotalEstimatedTime()));
			create(change, null);
			dao.persist(issue);
		}
	}

	@Transactional
	@Override
	public void changeTotalSpentTime(User user, Issue issue, int totalSpentTime) {
		int prevTotalSpentTime = issue.getTotalSpentTime();
		if (totalSpentTime != prevTotalSpentTime) {
			issue.setTotalSpentTime(totalSpentTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueTotalSpentTimeChangeData(prevTotalSpentTime, issue.getTotalSpentTime()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void changeDescription(User user, Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equals(description, prevDescription)) {
			if (description != null && description.length() > Issue.MAX_DESCRIPTION_LEN)
				throw new ExplicitException("Description too long");
			issue.setDescription(description);
			issue.setDescriptionRevisionCount(issue.getDescriptionRevisionCount() + 1);
			referenceChangeService.addReferenceChange(user, issue, description);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueDescriptionChangeData(prevDescription, issue.getDescription()));
			create(change, null);
			dao.persist(issue);

			var revision = new IssueDescriptionRevision();
			revision.setIssue(issue);
			revision.setUser(user);
			revision.setOldContent(prevDescription);
			revision.setNewContent(description);
			descriptionRevisionService.create(revision);
		}
	}
	
	@Transactional
	@Override
	public void changeConfidential(User user, Issue issue, boolean confidential) {
		boolean prevConfidential = issue.isConfidential();
		if (confidential != prevConfidential) {
			issue.setConfidential(confidential);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueConfidentialChangeData(prevConfidential, issue.isConfidential()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void addSchedule(User user, Issue issue, Iteration iteration) {
		addSchedule(user, issue, iteration, !user.isServiceAccount());
	}

	protected void addSchedule(User user, Issue issue, Iteration iteration, boolean sendNotifications) {
		var schedule = issue.addSchedule(iteration);
		if (schedule != null) {
			issueScheduleService.create(schedule);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setData(new IssueIterationAddData(iteration.getName()));
			change.setUser(user);
			create(change, null, sendNotifications);
		}
	}
	
	@Transactional
	@Override
	public void changeSchedule(User user, List<Issue> issues, Iteration addIteration, 
							   Iteration removeIteration, boolean sendNotifications) {
		for (var issue: issues) {
			if (addIteration != null)
				addSchedule(user, issue, addIteration, sendNotifications);
			if (removeIteration != null)
				removeSchedule(user, issue, removeIteration, sendNotifications);
		}
	}
	
	@Transactional
	@Override
	public void removeSchedule(User user, Issue issue, Iteration iteration) {
		removeSchedule(user, issue, iteration, !user.isServiceAccount());
	}

	@Transactional
	protected void removeSchedule(User user, Issue issue, Iteration iteration, boolean sendNotifications) {
		IssueSchedule schedule = issue.removeSchedule(iteration);
		if (schedule != null) {
			issueScheduleService.delete(schedule);
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setData(new IssueIterationRemoveData(iteration.getName()));
			change.setUser(user);
			create(change, null, sendNotifications);
		}
	}

	@Transactional
	@Override
	public void changeFields(User user, Issue issue, Map<String, Object> fieldValues) {
		Map<String, Input> prevFields = issue.getFieldInputs(); 
		issue.setFieldValues(fieldValues);
		issue.validateFields();
		if (!prevFields.equals(issue.getFieldInputs())) {			
			issueFieldService.saveFields(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueFieldChangeData(prevFields, issue.getFieldInputs()));
			create(change, null);
		}
	}
	
	@Transactional
	@Override
	public void changeState(User user, Issue issue, String state, Map<String, Object> fieldValues, 
			Collection<String> promptFields, Collection<String> removeFields, @Nullable String comment) {
		String prevState = issue.getState();
		if (!prevState.equals(state)) {
				Map<String, Input> prevFields = issue.getFieldInputs();
			
			issue.setState(state);
			issue.removeFields(removeFields);
			issue.setFieldValues(fieldValues);
			issue.addMissingFields(promptFields);
			issue.validateFields();
					
			issueFieldService.saveFields(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			change.setData(new IssueStateChangeData(prevState, issue.getState(), 
					prevFields, issue.getFieldInputs()));
			create(change, comment);
		}
	}
	
	@Transactional
	@Override
	public void batchUpdate(User user, Iterator<? extends Issue> issues, String state, 
			Boolean confidential, Collection<Iteration> iterations, 
			Map<String, Object> fieldValues, String comment, boolean sendNotifications) {
		while (issues.hasNext()) {
			Issue issue = issues.next();
			String prevState = issue.getState();
			boolean prevConfidential = issue.isConfidential();
			Collection<Iteration> prevIterations = issue.getIterations();
			Map<String, Input> prevFields = issue.getFieldInputs();
			if (state != null)
				issue.setState(state);
			
			if (confidential != null)
				issue.setConfidential(confidential);
			
			if (iterations != null) 
				issueScheduleService.syncIterations(issue, iterations);
			
			issue.setFieldValues(fieldValues);
			issue.validateFields();
			issueFieldService.saveFields(issue);

			if (!prevState.equals(issue.getState()) 
					|| prevConfidential != issue.isConfidential()
					|| !prevFields.equals(issue.getFieldInputs()) 
					|| !new HashSet<>(prevIterations).equals(new HashSet<>(issue.getIterations()))) {
				IssueChange change = new IssueChange();
				change.setIssue(issue);
				change.setUser(user);
				
				List<Iteration> prevIterationList = new ArrayList<>(prevIterations);
				prevIterationList.sort(new Iteration.DatesAndStatusComparator());
				List<Iteration> currentIterationList = new ArrayList<>(issue.getIterations());
				currentIterationList.sort(new Iteration.DatesAndStatusComparator());
				change.setData(new IssueBatchUpdateData(prevState, issue.getState(), 
						prevConfidential, issue.isConfidential(), prevIterationList, 
						currentIterationList, prevFields, issue.getFieldInputs()));
				create(change, comment, sendNotifications);
			}
		}
	}
	
	private List<TransitionSpec> getTransitionSpecs() {
		return OneDev.getInstance(SettingService.class).getIssueSetting().getTransitionSpecs();
	}
	
	@Transactional
	@Listen
	public void on(IssueChanged event) {
		if (event.getChange().getData() instanceof IssueStateChangeData) {
			try {
				SecurityUtils.bindAsSystem();
				var subject = SecurityUtils.getSubject();
				Issue issue = event.getIssue();
				
				IssueQueryParseOption option = new IssueQueryParseOption().withCurrentIssueCriteria(true);
				for (TransitionSpec transition: getTransitionSpecs()) {
					if (transition instanceof IssueStateTransitedSpec) {
						Project project = issue.getProject();
						ProjectScope projectScope = new ProjectScope(project, true, true);
						IssueStateTransitedSpec issueStateTransitedSpec = (IssueStateTransitedSpec) transition;
						IssueQuery query = IssueQuery.parse(project, issueStateTransitedSpec.getIssueQuery(), option, true);
						List<Criteria<Issue>> criterias = new ArrayList<>();
						
						List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
						for (String fromState: transition.getFromStates()) 
							fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
						
						if (!fromStateCriterias.isEmpty())
							criterias.add(Criteria.orCriterias(fromStateCriterias));
						if (query.getCriteria() != null)
							criterias.add(query.getCriteria());
						query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
						Issue.push(issue);
						try {
							for (Issue each: issueService.query(
									subject, projectScope, query, true, 0, MAX_VALUE)) {
								String message = "State changed as issue " + issue.getReference().toString(each.getProject()) 
										+ " transited to '" + issue.getState() + "'";
								changeState(SecurityUtils.getUser(subject), each, issueStateTransitedSpec.getToState(), 
										new HashMap<>(), new ArrayList<>(), transition.getRemoveFields(), message);
							}
						} finally {
							Issue.pop();
						}
					}      												
				}
			} catch (Exception e) {
				logger.error("Error changing issue state", e);
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(BuildFinished event) {
		try {
			SecurityUtils.bindAsSystem();
			var subject = SecurityUtils.getSubject();
			var user = SecurityUtils.getUser(subject);
			Build build = event.getBuild();

			IssueQueryParseOption option = new IssueQueryParseOption().withCurrentBuildCriteria(true);
			for (TransitionSpec transition: getTransitionSpecs()) {
				if (transition instanceof BuildSuccessfulSpec) {
					Project project = build.getProject();
					ProjectScope projectScope = new ProjectScope(project, true, true);
					BuildSuccessfulSpec buildSuccessfulSpec = (BuildSuccessfulSpec) transition;
					String branches = buildSuccessfulSpec.getBranches();
					ObjectId commitId = ObjectId.fromString(build.getCommitHash());
					if ((buildSuccessfulSpec.getJobNames() == null || PatternSet.parse(buildSuccessfulSpec.getJobNames()).matches(new StringMatcher(), build.getJobName())) 
							&& build.getStatus() == Build.Status.SUCCESSFUL
							&& (branches == null || project.isCommitOnBranches(commitId, PatternSet.parse(branches)))) {
						IssueQuery query = IssueQuery.parse(project, buildSuccessfulSpec.getIssueQuery(), option, true);
						List<Criteria<Issue>> criterias = new ArrayList<>();
						
						List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
						for (String fromState: transition.getFromStates()) 
							fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
						
						if (!fromStateCriterias.isEmpty())
							criterias.add(Criteria.orCriterias(fromStateCriterias));
						if (query.getCriteria() != null)
							criterias.add(query.getCriteria());
						query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
						Build.push(build);
						try {
							for (Issue issue: issueService.query(subject, projectScope, query, true, 0, MAX_VALUE)) {
								String message = "State changed as build " + build.getReference().toString(issue.getProject()) + " is successful";
								changeState(user, issue, buildSuccessfulSpec.getToState(), 
										new HashMap<>(), new ArrayList<>(), transition.getRemoveFields(), message);
							}
						} finally {
							Build.pop();
						}
					}
				}      												
			}
		} catch (Exception e) {
			logger.error("Error changing issue state", e);
		}
	}
	
	private void on(PullRequest request, Class<? extends PullRequestSpec> specClass) {
		try {
			SecurityUtils.bindAsSystem();
			var subject = SecurityUtils.getSubject();
			var user = SecurityUtils.getUser(subject);
			Matcher matcher = new PathMatcher();
			Project project = request.getTargetProject();
			ProjectScope projectScope = new ProjectScope(project, true, true);
			IssueQueryParseOption option = new IssueQueryParseOption().withCurrentPullRequestCriteria(true);
			for (TransitionSpec transition: getTransitionSpecs()) {
				if (transition.getClass() == specClass) {
					PullRequestSpec pullRequestSpec = (PullRequestSpec) transition;
					if (pullRequestSpec.getBranches() == null || PatternSet.parse(pullRequestSpec.getBranches()).matches(matcher, request.getTargetBranch())) {
						IssueQuery query = IssueQuery.parse(project, pullRequestSpec.getIssueQuery(), option, true);
						List<Criteria<Issue>> criterias = new ArrayList<>();
						
						List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
						for (String fromState: transition.getFromStates()) 
							fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
						
						if (!fromStateCriterias.isEmpty())
							criterias.add(Criteria.orCriterias(fromStateCriterias));
						if (query.getCriteria() != null)
							criterias.add(query.getCriteria());
						query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
						PullRequest.push(request);
						try {
							for (Issue issue: issueService.query(subject, projectScope, query, true, 0, MAX_VALUE)) {
								String statusName = request.getStatus().toString().toLowerCase();
								changeState(user, issue, pullRequestSpec.getToState(), 
										new HashMap<>(), new ArrayList<>(), transition.getRemoveFields(), 
										"State changed as pull request " + request.getReference().toString(issue.getProject()) + " is " + statusName);
							}
						} finally {
							PullRequest.pop();
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error changing issue state", e);
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChanged event) { 
		if (event.getChange().getData() instanceof PullRequestMergeData) 
			on(event.getRequest(), PullRequestMergedSpec.class);
		else if (event.getChange().getData() instanceof PullRequestDiscardData) 
			on(event.getRequest(), PullRequestDiscardedSpec.class);
		else if (event.getChange().getData() instanceof PullRequestReopenData) 
			on(event.getRequest(), PullRequestOpenedSpec.class);
	}
	
	@Transactional
	@Listen
	public void on(io.onedev.server.event.project.pullrequest.PullRequestOpened event) {
		on(event.getRequest(), PullRequestOpenedSpec.class);
	}
	
	@Transactional
	@Listen
	public void on(RefUpdated event) {
		String refName = event.getRefName();
		String branchName = GitUtils.ref2branch(refName);
		ObjectId newCommitId = event.getNewCommitId();
		if (!newCommitId.equals(ObjectId.zeroId()) && branchName != null) {
			/*
			 * Transit states of issues fixed by commits under the new branch. Note that ideally 
			 * we should also transit states of issues fixed by builds under the new branch. 
			 * However that will be very cost. To work around this, when a branch is pushed, new 
			 * builds should be running on that branch in order to get issue states transited 
			 */
			Project project = event.getProject();
			Long projectId = project.getId();
			ObjectId oldCommitId = event.getOldCommitId();
			
			try {
				SecurityUtils.bindAsSystem();
				var subject = SecurityUtils.getSubject();
				var user = SecurityUtils.getUser(subject);
				ProjectScope projectScope = new ProjectScope(project, true, true);
				Map<Long, RevCommit> fixedIssueIds = new HashMap<>();
				Repository repository = projectService.getRepository(projectId);
				String commitMessage;
				try (RevWalk revWalk = new RevWalk(repository)) {
					var newCommit = revWalk.parseCommit(newCommitId);
					commitMessage = newCommit.getFullMessage();
					revWalk.markStart(newCommit);
					if (oldCommitId.equals(ObjectId.zeroId())) {
						/*
						 * In case a new branch is pushed, we only process new commits not in any existing branches
						 * for performance reason. This is reasonable as state of fixed issues by commits in 
						 * existing branches most probably is already transited        
						 */
						for (Ref ref: repository.getRefDatabase().getRefsByPrefix(Constants.R_HEADS)) {
							if (!ref.getName().equals(refName))
								revWalk.markUninteresting(revWalk.lookupCommit(ref.getObjectId()));
						}
					} else {
						revWalk.markUninteresting(revWalk.lookupCommit(oldCommitId));
					}
					RevCommit commit;
					while ((commit = revWalk.next()) != null) {
						for (Long issueId: project.parseFixedIssueIds(commit.getFullMessage())) 
							fixedIssueIds.put(issueId, commit);							
						if (fixedIssueIds.size() > MAX_FIXED_ISSUES)
							break;
					}
				} 
				
				var option = new IssueQueryParseOption().withCurrentCommitCriteria(true);
				for (var transition: getTransitionSpecs()) {
					if (transition instanceof BranchUpdatedSpec) {
						var branchUpdatedSpec = (BranchUpdatedSpec) transition;
						var branches = branchUpdatedSpec.getBranches();
						var commitMessages = branchUpdatedSpec.getCommitMessages();
						var branchMatcher = new PathMatcher();
						var commitMessagesMatcher = new StringMatcher();
						if ((branches == null || PatternSet.parse(branches).matches(branchMatcher, branchName))
								&& (commitMessages == null || PatternSet.parse(commitMessages).matches(commitMessagesMatcher, commitMessage))) {
							IssueQuery query = IssueQuery.parse(project, branchUpdatedSpec.getIssueQuery(), option, true);
							List<Criteria<Issue>> criterias = new ArrayList<>();
							
							List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
							for (String fromState: transition.getFromStates()) 
								fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
							
							if (!fromStateCriterias.isEmpty())
								criterias.add(Criteria.orCriterias(fromStateCriterias));
							if (query.getCriteria() != null)
								criterias.add(query.getCriteria());
							query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
							ProjectScopedCommit.push(new ProjectScopedCommit(project, newCommitId) {

								@Override
								public Collection<Long> getFixedIssueIds() {
									return fixedIssueIds.keySet();
								}
								
							});
							try {
								for (Issue issue: issueService.query(subject, projectScope, query, true, 0, MAX_VALUE)) {
									var commit = fixedIssueIds.get(issue.getId());
									if (commit != null) {
										String commitFQN = commit.name();
										if (!project.equals(issue.getProject()))
											commitFQN = project.getPath() + ":" + commitFQN;
										changeState(user, issue, branchUpdatedSpec.getToState(), 
												new HashMap<>(), new ArrayList<>(), transition.getRemoveFields(),
												"State changed as code fixing the issue is committed (" + commitFQN + ")");
									}
								}
							} finally {
								ProjectScopedCommit.pop();
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error changing issue state", e);
			}
		}
	}

	@Override
	public void execute() {
		batchWorkExecutionService.submit(new BatchWorker("no-activity-issue-transition") {

			@Override
			public void doWorks(List<Prioritized> works) {
				transactionService.run(() -> {
					if (clusterService.isLeaderServer()) {
						var subject = SecurityUtils.getSubject();
						var user = SecurityUtils.getUser(subject);
						IssueQueryParseOption option = new IssueQueryParseOption();
						for (TransitionSpec transition: getTransitionSpecs()) {
							if (transition instanceof NoActivitySpec) {
								NoActivitySpec noActivitySpec = (NoActivitySpec) transition;
								IssueQuery query = IssueQuery.parse(null, noActivitySpec.getIssueQuery(), option, false);
								List<Criteria<Issue>> criterias = new ArrayList<>();
								
								List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
								for (String fromState: transition.getFromStates()) 
									fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
								
								if (!fromStateCriterias.isEmpty())
									criterias.add(Criteria.orCriterias(fromStateCriterias));
								if (query.getCriteria() != null)
									criterias.add(query.getCriteria());
								
								criterias.add(new LastActivityDateCriteria(
										new DateTime().minusDays(noActivitySpec.getDays()).toDate(), 
										IssueQueryLexer.IsUntil));
								
								query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
								
								for (Issue issue: issueService.query(subject, null, query, true, 0, MAX_VALUE)) {
									changeState(user, issue, noActivitySpec.getToState(), new HashMap<>(), new ArrayList<>(), 
											transition.getRemoveFields(), null);
								}
							}
						}
					}									
				});
			}
			
		}, new Prioritized(NO_ACTIVITY_TRANSITION_PRIORITY));

	}
	
	@Sessional
	@Override
	public List<IssueChange> queryAfter(Long projectId, Long afterChangeId, int count) {
		EntityCriteria<IssueChange> criteria = newCriteria();
		criteria.createCriteria("issue").add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt("id", afterChangeId));
		criteria.addOrder(Order.asc("id"));
		return query(criteria, 0, count);
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}

	@Transactional
	@Override
	public void changeIterations(User user, Issue issue, Collection<Iteration> iterations) {
		Collection<Iteration> prevIterations = new HashSet<>(issue.getIterations());
		if (!prevIterations.equals(new HashSet<>(iterations))) {
			issueScheduleService.syncIterations(issue, iterations);
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(user);
			
			List<Iteration> prevIterationList = new ArrayList<>(prevIterations);
			prevIterationList.sort(new Iteration.DatesAndStatusComparator());
			List<Iteration> currentIterationList = new ArrayList<>(iterations);
			currentIterationList.sort(new Iteration.DatesAndStatusComparator());
			change.setData(new IssueIterationChangeData(prevIterationList, currentIterationList));
			create(change, null);
		}
		
	}
	
	private void logLinkedSideChange(LinkSpec spec, Issue issue, @Nullable Issue prevLinkedIssue, 
			@Nullable Issue linkedIssue, boolean opposite) {
		String linkName;
		if (spec.getOpposite() != null) 
			linkName = opposite?spec.getName():spec.getOpposite().getName();
		else 
			linkName = spec.getName();
		if (prevLinkedIssue != null) {
			IssueChange change = new IssueChange();
			change.setIssue(prevLinkedIssue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueLinkRemoveData(linkName, !opposite, getLinkedIssueNumber(prevLinkedIssue, issue)));
			create(change, null);
		} 
		if (linkedIssue != null) {
			IssueChange change = new IssueChange();
			change.setIssue(linkedIssue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueLinkAddData(linkName, !opposite, getLinkedIssueNumber(linkedIssue, issue)));
			create(change, null);
		}
	}

	private String getLinkedIssueNumber(Issue issue, Issue linkedIssue) {
		return linkedIssue.getReference().toString(issue.getProject());
	}
	
	@Transactional
	@Override
	public void addLink(User user, LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite) {
		Collection<Issue> linkedIssues = issue.findLinkedIssues(spec, opposite);
		linkedIssues.add(linkedIssue);
		issueLinkService.syncLinks(spec, issue, linkedIssues, opposite);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setUser(user);
	
		String linkName = spec.getName(opposite);
		IssueLinkAddData data = new IssueLinkAddData(linkName, opposite, getLinkedIssueNumber(issue, linkedIssue));
		change.setData(data);
		create(change, null);
		
		logLinkedSideChange(spec, issue, null, linkedIssue, opposite);
	}

	@Transactional
	@Override
	public void removeLink(User user, LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite) {
		Collection<Issue> linkedIssues = issue.findLinkedIssues(spec, opposite);
		linkedIssues.remove(linkedIssue);
		issueLinkService.syncLinks(spec, issue, linkedIssues, opposite);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setUser(user);
		
		String linkName = spec.getName(opposite);
		IssueLinkRemoveData data = new IssueLinkRemoveData(linkName, opposite, getLinkedIssueNumber(issue, linkedIssue));
		change.setData(data);
		create(change, null);
		
		logLinkedSideChange(spec, issue, linkedIssue, null, opposite);
	}

	@Transactional
	@Override
	public void changeLink(User user, LinkSpec spec, Issue issue, Issue prevLinkedIssue, 
			Issue linkedIssue, boolean opposite) {
		if (prevLinkedIssue != null)
			removeLink(user, spec, issue, prevLinkedIssue, opposite);
		if (linkedIssue != null)
			addLink(user, spec, issue, linkedIssue, opposite);
	}

	@Sessional
	@Override
	public List<IssueChange> query(User submitter, Date fromDate, Date toDate) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueChange> query = builder.createQuery(IssueChange.class);
		From<IssueChange, IssueChange> root = query.from(IssueChange.class);
		
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.equal(root.get(IssueChange.PROP_USER), submitter));
		predicates.add(builder.greaterThanOrEqualTo(root.get(IssueChange.PROP_DATE), fromDate));
		predicates.add(builder.lessThanOrEqualTo(root.get(IssueChange.PROP_DATE), toDate));
			
		query.where(predicates.toArray(new Predicate[0]));
		
		return getSession().createQuery(query).getResultList();
	}

}
