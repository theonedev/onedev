package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.*;
import io.onedev.server.entityreference.EntityReferenceManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.build.BuildFinished;
import io.onedev.server.event.project.issue.IssueChanged;
import io.onedev.server.event.project.pullrequest.PullRequestChanged;
import io.onedev.server.event.project.pullrequest.PullRequestOpened;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.*;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.issue.changedata.*;
import io.onedev.server.model.support.issue.transitiontrigger.*;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.issue.*;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class DefaultIssueChangeManager extends BaseEntityManager<IssueChange>
		implements IssueChangeManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueChangeManager.class);
	
	private static final int MAX_FIXED_ISSUES = 1000;
	
	private final IssueManager issueManager;
	
	private final IssueFieldManager issueFieldManager;
	
	private final ProjectManager projectManager;
	
	private final IssueScheduleManager issueScheduleManager;
	
	private final IssueLinkManager issueLinkManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TaskScheduler taskScheduler;
	
	private final ClusterManager clusterManager;
	
	private final EntityReferenceManager entityReferenceManager;
	
	private String taskId;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, IssueManager issueManager, IssueFieldManager issueFieldManager,
									 ProjectManager projectManager, ListenerRegistry listenerRegistry, 
									 TaskScheduler taskScheduler, IssueScheduleManager issueScheduleManager, 
									 IssueLinkManager issueLinkManager, ClusterManager clusterManager, 
									 EntityReferenceManager entityReferenceManager) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldManager = issueFieldManager;
		this.projectManager = projectManager;
		this.listenerRegistry = listenerRegistry;
		this.taskScheduler = taskScheduler;
		this.issueScheduleManager = issueScheduleManager;
		this.issueLinkManager = issueLinkManager;
		this.clusterManager = clusterManager;
		this.entityReferenceManager = entityReferenceManager;
	}

	@Transactional
	@Override
	public void create(IssueChange change, @Nullable String note) {
		Preconditions.checkState(change.isNew());
		change.getIssue().getChanges().add(change);
		dao.persist(change);
		if (note != null && change.getUser() != null) {
			IssueComment comment = new IssueComment();
			comment.setContent(note);
			comment.setUser(change.getUser());
			comment.setIssue(change.getIssue());
			comment.setDate(change.getDate());
			dao.persist(comment);
			comment.getIssue().setCommentCount(comment.getIssue().getCommentCount()+1);
		}

		listenerRegistry.post(new IssueChanged(change, note));
	}
	
	@Transactional
	@Override
	public void changeTitle(Issue issue, String title) {
		String prevTitle = issue.getTitle();
		if (!title.equals(prevTitle)) {
			issue.setTitle(title);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueTitleChangeData(prevTitle, issue.getTitle()));
			create(change, null);
			dao.persist(issue);
		}
	}

	@Transactional
	@Override
	public void changeOwnEstimatedTime(Issue issue, int ownEstimatedTime) {
		int prevOwnEstimatedTime = issue.getOwnEstimatedTime();
		if (ownEstimatedTime != prevOwnEstimatedTime) {
			issue.setOwnEstimatedTime(ownEstimatedTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueOwnEstimatedTimeChangeData(prevOwnEstimatedTime, issue.getOwnEstimatedTime()));
			create(change, null);
			dao.persist(issue);
		}
	}

	@Transactional
	@Override
	public void changeOwnSpentTime(Issue issue, int ownSpentTime) {
		int prevOwnSpentTime = issue.getOwnSpentTime();
		if (ownSpentTime != prevOwnSpentTime) {
			issue.setOwnSpentTime(ownSpentTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueOwnSpentTimeChangeData(prevOwnSpentTime, issue.getOwnSpentTime()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void changeTotalEstimatedTime(Issue issue, int totalEstimatedTime) {
		int prevTotalEstimatedTime = issue.getTotalEstimatedTime();
		if (totalEstimatedTime != prevTotalEstimatedTime) {
			issue.setTotalEstimatedTime(totalEstimatedTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueTotalEstimatedTimeChangeData(prevTotalEstimatedTime, issue.getTotalEstimatedTime()));
			create(change, null);
			dao.persist(issue);
		}
	}

	@Transactional
	@Override
	public void changeTotalSpentTime(Issue issue, int totalSpentTime) {
		int prevTotalSpentTime = issue.getTotalSpentTime();
		if (totalSpentTime != prevTotalSpentTime) {
			issue.setTotalSpentTime(totalSpentTime);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueTotalSpentTimeChangeData(prevTotalSpentTime, issue.getTotalSpentTime()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void changeDescription(Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equals(description, prevDescription)) {
			if (description != null && description.length() > Issue.MAX_DESCRIPTION_LEN)
				throw new ExplicitException("Description too long");
			issue.setDescription(description);
			entityReferenceManager.addReferenceChange(SecurityUtils.getUser(), issue, description);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueDescriptionChangeData(prevDescription, issue.getDescription()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void changeConfidential(Issue issue, boolean confidential) {
		boolean prevConfidential = issue.isConfidential();
		if (confidential != prevConfidential) {
			issue.setConfidential(confidential);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueConfidentialChangeData(prevConfidential, issue.isConfidential()));
			create(change, null);
			dao.persist(issue);
		}
	}
	
	@Transactional
	@Override
	public void addSchedule(Issue issue, Milestone milestone) {
		issueScheduleManager.create(issue.addSchedule(milestone));
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setData(new IssueMilestoneAddData(milestone.getName()));
		change.setUser(SecurityUtils.getUser());
		create(change, null);
	}
	
	@Transactional
	@Override
	public void removeSchedule(Issue issue, Milestone milestone) {
		IssueSchedule schedule = issue.removeSchedule(milestone);
		if (schedule != null) {
			issueScheduleManager.delete(schedule);
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setData(new IssueMilestoneRemoveData(milestone.getName()));
			change.setUser(SecurityUtils.getUser());
			create(change, null);
		}
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Map<String, Object> fieldValues) {
		Map<String, Input> prevFields = issue.getFieldInputs(); 
		issue.setFieldValues(fieldValues);
		if (!prevFields.equals(issue.getFieldInputs())) {
			issueFieldManager.saveFields(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueFieldChangeData(prevFields, issue.getFieldInputs()));
			create(change, null);
		}
	}
	
	@Transactional
	@Override
	public void changeState(Issue issue, String state, Map<String, Object> fieldValues, 
			Collection<String> removeFields, @Nullable String comment) {
		String prevState = issue.getState();
		Map<String, Input> prevFields = issue.getFieldInputs();
		
		issue.setState(state);
		issue.removeFields(removeFields);
		issue.setFieldValues(fieldValues);
		
		issueFieldManager.saveFields(issue);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setUser(SecurityUtils.getUser());
		change.setData(new IssueStateChangeData(prevState, issue.getState(), 
				prevFields, issue.getFieldInputs()));
		create(change, comment);
	}
	
	@Transactional
	@Override
	public void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, @Nullable Boolean confidential,
			@Nullable Collection<Milestone> milestones, Map<String, Object> fieldValues, @Nullable String comment) {
		while (issues.hasNext()) {
			Issue issue = issues.next();
			String prevState = issue.getState();
			boolean prevConfidential = issue.isConfidential();
			Collection<Milestone> prevMilestones = issue.getMilestones();
			Map<String, Input> prevFields = issue.getFieldInputs();
			if (state != null)
				issue.setState(state);
			
			if (confidential != null)
				issue.setConfidential(confidential);
			
			if (milestones != null) 
				issueScheduleManager.syncMilestones(issue, milestones);
			
			issue.setFieldValues(fieldValues);
			issueFieldManager.saveFields(issue);

			if (!prevState.equals(issue.getState()) 
					|| prevConfidential != issue.isConfidential()
					|| !prevFields.equals(issue.getFieldInputs()) 
					|| !new HashSet<>(prevMilestones).equals(new HashSet<>(issue.getMilestones()))) {
				IssueChange change = new IssueChange();
				change.setIssue(issue);
				change.setUser(SecurityUtils.getUser());
				
				List<Milestone> prevMilestoneList = new ArrayList<>(prevMilestones);
				prevMilestoneList.sort(new Milestone.DatesAndStatusComparator());
				List<Milestone> currentMilestoneList = new ArrayList<>(issue.getMilestones());
				currentMilestoneList.sort(new Milestone.DatesAndStatusComparator());
				change.setData(new IssueBatchUpdateData(prevState, issue.getState(), 
						prevConfidential, issue.isConfidential(), prevMilestoneList, 
						currentMilestoneList, prevFields, issue.getFieldInputs()));
				create(change, comment);
			}
		}
	}
	
	private List<TransitionSpec> getTransitionSpecs() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting().getTransitionSpecs();
	}
	
	@Transactional
	@Listen
	public void on(IssueChanged event) {
		if (event.getChange().getData() instanceof IssueStateChangeData) {
			try {
				SecurityUtils.bindAsSystem();
				Issue issue = event.getIssue();
				
				IssueQueryParseOption option = new IssueQueryParseOption().withCurrentIssueCriteria(true);
				for (TransitionSpec transition: getTransitionSpecs()) {
					if (transition.getTrigger() instanceof StateTransitionTrigger) {
						Project project = issue.getProject();
						ProjectScope projectScope = new ProjectScope(project, true, true);
						StateTransitionTrigger trigger = (StateTransitionTrigger) transition.getTrigger();
						if (trigger.getStates().contains(issue.getState())) {
							IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), option, true);
							List<Criteria<Issue>> criterias = new ArrayList<>();
							
							List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
							for (String fromState: transition.getFromStates()) 
								fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
							
							criterias.add(Criteria.orCriterias(fromStateCriterias));
							if (query.getCriteria() != null)
								criterias.add(query.getCriteria());
							query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
							Issue.push(issue);
							try {
								for (Issue each: issueManager.query(
										projectScope, query, true, 0, Integer.MAX_VALUE)) {
									String message = "State changed as issue #" + issue.getNumber() 
											+ " transited to '" + issue.getState() + "'";
									changeState(each, transition.getToState(), new HashMap<>(), 
											transition.getRemoveFields(), message);
								}
							} finally {
								Issue.pop();
							}
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
			Build build = event.getBuild();

			IssueQueryParseOption option = new IssueQueryParseOption().withCurrentBuildCriteria(true);
			for (TransitionSpec transition: getTransitionSpecs()) {
				if (transition.getTrigger() instanceof BuildSuccessfulTrigger) {
					Project project = build.getProject();
					ProjectScope projectScope = new ProjectScope(project, true, true);
					BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) transition.getTrigger();
					String branches = trigger.getBranches();
					ObjectId commitId = ObjectId.fromString(build.getCommitHash());
					if ((trigger.getJobNames() == null || PatternSet.parse(trigger.getJobNames()).matches(new StringMatcher(), build.getJobName())) 
							&& build.getStatus() == Build.Status.SUCCESSFUL
							&& (branches == null || project.isCommitOnBranches(commitId, PatternSet.parse(branches)))) {
						IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), option, true);
						List<Criteria<Issue>> criterias = new ArrayList<>();
						
						List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
						for (String fromState: transition.getFromStates()) 
							fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
						
						criterias.add(Criteria.orCriterias(fromStateCriterias));
						if (query.getCriteria() != null)
							criterias.add(query.getCriteria());
						query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
						Build.push(build);
						try {
							for (Issue issue: issueManager.query(projectScope, query, true, 0, Integer.MAX_VALUE)) {
								String message = "State changed as build #" + build.getNumber() + " is successful";
								changeState(issue, transition.getToState(), new HashMap<>(), 
										transition.getRemoveFields(), message);
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
	
	private void on(PullRequest request, Class<? extends PullRequestTrigger> triggerClass) {
		try {
			SecurityUtils.bindAsSystem();
			Matcher matcher = new PathMatcher();
			Project project = request.getTargetProject();
			ProjectScope projectScope = new ProjectScope(project, true, true);
			IssueQueryParseOption option = new IssueQueryParseOption().withCurrentPullRequestCriteria(true);
			for (TransitionSpec transition: getTransitionSpecs()) {
				if (transition.getTrigger().getClass() == triggerClass) {
					PullRequestTrigger trigger = (PullRequestTrigger) transition.getTrigger();
					if (trigger.getBranches() == null || PatternSet.parse(trigger.getBranches()).matches(matcher, request.getTargetBranch())) {
						IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), option, true);
						List<Criteria<Issue>> criterias = new ArrayList<>();
						
						List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
						for (String fromState: transition.getFromStates()) 
							fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
						
						criterias.add(Criteria.orCriterias(fromStateCriterias));
						if (query.getCriteria() != null)
							criterias.add(query.getCriteria());
						query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
						PullRequest.push(request);
						try {
							for (Issue issue: issueManager.query(projectScope, query, true, 0, Integer.MAX_VALUE)) {
								String statusName = request.getStatus().toString().toLowerCase();
								changeState(issue, transition.getToState(), new HashMap<>(), 
										transition.getRemoveFields(), 
										"State changed as pull request #" + request.getNumber() + " is " + statusName);
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
			on(event.getRequest(), MergePullRequestTrigger.class);
		else if (event.getChange().getData() instanceof PullRequestDiscardData) 
			on(event.getRequest(), DiscardPullRequestTrigger.class);
		else if (event.getChange().getData() instanceof PullRequestReopenData) 
			on(event.getRequest(), OpenPullRequestTrigger.class);
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		on(event.getRequest(), OpenPullRequestTrigger.class);
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
				ProjectScope projectScope = new ProjectScope(project, true, true);
				Map<Long, RevCommit> fixedIssueIds = new HashMap<>();
				Repository repository = projectManager.getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository)) {
					revWalk.markStart(revWalk.lookupCommit(newCommitId));
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
				
				IssueQueryParseOption option = new IssueQueryParseOption().withCurrentCommitCriteria(true);
				for (TransitionSpec transition: getTransitionSpecs()) {
					if (transition.getTrigger() instanceof BranchUpdateTrigger) {
						BranchUpdateTrigger trigger = (BranchUpdateTrigger) transition.getTrigger();
						String branches = trigger.getBranches();
						Matcher matcher = new PathMatcher();
						if (branches == null || PatternSet.parse(branches).matches(matcher, branchName)) {
							IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), option, true);
							List<Criteria<Issue>> criterias = new ArrayList<>();
							
							List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
							for (String fromState: transition.getFromStates()) 
								fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
							
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
								for (Issue issue: issueManager.query(projectScope, query, true, 0, Integer.MAX_VALUE)) {
									var commit = fixedIssueIds.get(issue.getId());
									if (commit != null) {
										String commitFQN = commit.name();
										if (!project.equals(issue.getProject()))
											commitFQN = project.getPath() + ":" + commitFQN;
										changeState(issue, transition.getToState(), new HashMap<>(),
												transition.getRemoveFields(),
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

	@Transactional
	@Override
	public void execute() {
		if (clusterManager.isLeaderServer()) {
			IssueQueryParseOption option = new IssueQueryParseOption();
			for (TransitionSpec transition: getTransitionSpecs()) {
				if (transition.getTrigger() instanceof NoActivityTrigger) {
					NoActivityTrigger trigger = (NoActivityTrigger) transition.getTrigger();
					IssueQuery query = IssueQuery.parse(null, trigger.getIssueQuery(), option, false);
					List<Criteria<Issue>> criterias = new ArrayList<>();
					
					List<Criteria<Issue>> fromStateCriterias = new ArrayList<>();
					for (String fromState: transition.getFromStates()) 
						fromStateCriterias.add(new StateCriteria(fromState, IssueQueryLexer.Is));
					
					criterias.add(Criteria.orCriterias(fromStateCriterias));
					if (query.getCriteria() != null)
						criterias.add(query.getCriteria());
					
					criterias.add(new LastActivityDateCriteria(
							new DateTime().minusDays(trigger.getDays()).toDate(), 
							IssueQueryLexer.IsUntil));
					
					query = new IssueQuery(Criteria.andCriterias(criterias), new ArrayList<>());
					
					for (Issue issue: issueManager.query(null, query, true, 0, Integer.MAX_VALUE)) {
						changeState(issue, transition.getToState(), new HashMap<>(), 
								transition.getRemoveFields(), null);
					}
				}
			}
		}
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
		return CronScheduleBuilder.dailyAtHourAndMinute(1, 0);
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
	public void changeMilestones(Issue issue, Collection<Milestone> milestones) {
		Collection<Milestone> prevMilestones = new HashSet<>(issue.getMilestones());
		if (!prevMilestones.equals(new HashSet<>(milestones))) {
			issueScheduleManager.syncMilestones(issue, milestones);
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setUser(SecurityUtils.getUser());
			
			List<Milestone> prevMilestoneList = new ArrayList<>(prevMilestones);
			prevMilestoneList.sort(new Milestone.DatesAndStatusComparator());
			List<Milestone> currentMilestoneList = new ArrayList<>(milestones);
			currentMilestoneList.sort(new Milestone.DatesAndStatusComparator());
			change.setData(new IssueMilestoneChangeData(prevMilestoneList, currentMilestoneList));
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

	@Nullable
	private String getLinkedIssueNumber(Issue issue, Issue linkedIssue) {
		if (linkedIssue.getNumberScope().equals(issue.getNumberScope()))
			return "#" + linkedIssue.getNumber();
		else
			return linkedIssue.getProject() + "#" + linkedIssue.getNumber();
	}
	
	@Transactional
	@Override
	public void addLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite) {
		Collection<Issue> linkedIssues = issue.findLinkedIssues(spec, opposite);
		linkedIssues.add(linkedIssue);
		issueLinkManager.syncLinks(spec, issue, linkedIssues, opposite);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setUser(SecurityUtils.getUser());
	
		String linkName = spec.getName(opposite);
		IssueLinkAddData data = new IssueLinkAddData(linkName, opposite, getLinkedIssueNumber(issue, linkedIssue));
		change.setData(data);
		create(change, null);
		
		logLinkedSideChange(spec, issue, null, linkedIssue, opposite);
	}

	@Transactional
	@Override
	public void removeLink(LinkSpec spec, Issue issue, Issue linkedIssue, boolean opposite) {
		Collection<Issue> linkedIssues = issue.findLinkedIssues(spec, opposite);
		linkedIssues.remove(linkedIssue);
		issueLinkManager.syncLinks(spec, issue, linkedIssues, opposite);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setUser(SecurityUtils.getUser());
		
		String linkName = spec.getName(opposite);
		IssueLinkRemoveData data = new IssueLinkRemoveData(linkName, opposite, getLinkedIssueNumber(issue, linkedIssue));
		change.setData(data);
		create(change, null);
		
		logLinkedSideChange(spec, issue, linkedIssue, null, opposite);
	}

	@Transactional
	@Override
	public void changeLink(LinkSpec spec, Issue issue, @Nullable Issue prevLinkedIssue, @Nullable Issue linkedIssue, boolean opposite) {
		if (prevLinkedIssue != null)
			removeLink(spec, issue, prevLinkedIssue, opposite);
		if (linkedIssue != null)
			addLink(spec, issue, linkedIssue, opposite);
	}
}
