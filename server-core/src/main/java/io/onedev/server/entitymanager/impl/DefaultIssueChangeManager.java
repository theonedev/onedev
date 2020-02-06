package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Optional;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.git.GitUtils;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.issue.transitiontrigger.BranchUpdateTrigger;
import io.onedev.server.issue.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.issue.transitiontrigger.DiscardPullRequest;
import io.onedev.server.issue.transitiontrigger.MergePullRequest;
import io.onedev.server.issue.transitiontrigger.OpenPullRequest;
import io.onedev.server.issue.transitiontrigger.PullRequestTrigger;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueCommittedData;
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.model.support.issue.changedata.IssueMilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.IssuePullRequestDiscardedData;
import io.onedev.server.model.support.issue.changedata.IssuePullRequestMergedData;
import io.onedev.server.model.support.issue.changedata.IssuePullRequestOpenedData;
import io.onedev.server.model.support.issue.changedata.IssuePullRequestReopenedData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTitleChangeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.entity.issue.IssueCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.StateCriteria;
import io.onedev.server.util.Input;
import io.onedev.server.util.ProjectAwareCommit;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultIssueChangeManager extends AbstractEntityManager<IssueChange>
		implements IssueChangeManager {

	private final IssueManager issueManager;
	
	private final IssueFieldManager issueFieldManager;
	
	private final TransactionManager transactionManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final BuildManager buildManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, TransactionManager transactionManager, 
			IssueManager issueManager,  IssueFieldManager issueFieldManager,
			ProjectManager projectManager, BuildManager buildManager,
			PullRequestManager pullRequestManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldManager = issueFieldManager;
		this.transactionManager = transactionManager;
		this.projectManager = projectManager;
		this.pullRequestManager = pullRequestManager;
		this.buildManager = buildManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void save(IssueChange change) {
		dao.persist(change);
		listenerRegistry.post(new IssueChangeEvent(change));
	}
	
	@Transactional
	@Override
	public void changeTitle(Issue issue, String title) {
		String prevTitle = issue.getTitle();
		if (!title.equals(prevTitle)) {
			issue.setTitle(title);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueTitleChangeData(prevTitle, issue.getTitle()));
			save(change);
		}
	}

	private void checkBuild(TransitionSpec transition, Build build) {
		if (transition.getTrigger() instanceof BuildSuccessfulTrigger) {
			Project project = build.getProject();
			BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) transition.getTrigger();
			String branches = trigger.getBranches();
			ObjectId commitId = ObjectId.fromString(build.getCommitHash());
			if ((trigger.getJobNames() == null || PatternSet.parse(trigger.getJobNames()).matches(new StringMatcher(), build.getJobName())) 
					&& build.getStatus() == Build.Status.SUCCESSFUL
					&& (branches == null || project.isCommitOnBranches(commitId, branches))) {
				IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), true, false, true, false, false);
				List<IssueCriteria> criterias = new ArrayList<>();
				
				List<IssueCriteria> fromStateCriterias = new ArrayList<>();
				for (String fromState: transition.getFromStates()) 
					fromStateCriterias.add(new StateCriteria(fromState));
				
				criterias.add(IssueCriteria.or(fromStateCriterias));
				criterias.add(query.getCriteria());
				query = new IssueQuery(IssueCriteria.and(criterias), new ArrayList<>());
				Build.push(build);
				try {
					for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
						issue.removeFields(transition.getRemoveFields());
						changeState(issue, transition.getToState(), new HashMap<>(), null);
					}
				} finally {
					Build.pop();
				}
			}
		}               
	}

	@Transactional
	@Override
	public void changeMilestone(Issue issue, @Nullable Milestone milestone) {
		Milestone prevMilestone = issue.getMilestone();
		if (!Objects.equal(prevMilestone, milestone)) {
			issue.setMilestone(milestone);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueMilestoneChangeData(prevMilestone, issue.getMilestone()));
			save(change);
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
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueFieldChangeData(prevFields, issue.getFieldInputs()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeState(Issue issue, String state, Map<String, Object> fieldValues, @Nullable String comment) {
		String prevState = issue.getState();
		Map<String, Input> prevFields = issue.getFieldInputs();
		issue.setState(state);

		issue.setFieldValues(fieldValues);
		
		issueFieldManager.saveFields(issue);
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setDate(new Date());
		change.setUser(SecurityUtils.getUser());
		change.setData(new IssueStateChangeData(prevState, issue.getState(), 
				prevFields, issue.getFieldInputs(), comment));
		save(change);
	}
	
	@Transactional
	@Override
	public void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, 
			@Nullable Optional<Milestone> milestone, Map<String, Object> fieldValues, 
			@Nullable String comment) {
		while (issues.hasNext()) {
			Issue issue = issues.next();
			String prevState = issue.getState();
			Milestone prevMilestone = issue.getMilestone();
			Map<String, Input> prevFields = issue.getFieldInputs();
			if (state != null)
				issue.setState(state);
			if (milestone != null)
				issue.setMilestone(milestone.orNull());
			
			issue.setFieldValues(fieldValues);
			issueFieldManager.saveFields(issue);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueBatchUpdateData(prevState, issue.getState(), prevMilestone, 
					issue.getMilestone(), prevFields, issue.getFieldInputs(), comment));
			
			save(change);
		}
	}
	
	@Transactional
	@Listen
	public void on(BuildFinished event) {
		Long buildId = event.getBuild().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				transactionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						SecurityUtils.bindAsSystem();
						Build build = buildManager.load(buildId);
						for (TransitionSpec transition: build.getProject().getIssueSetting().getTransitionSpecs(true)) 
							checkBuild(transition, build);
					}
					
				});
			}
		});
	}
	
	private void on(PullRequest request, Class<? extends PullRequestTrigger> triggerClass) {
		Long requestId = request.getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				transactionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						SecurityUtils.bindAsSystem();
						Matcher matcher = new PathMatcher();
						PullRequest request = pullRequestManager.load(requestId);
						Project project = request.getTargetProject();
						for (TransitionSpec transition: project.getIssueSetting().getTransitionSpecs(true)) {
							if (transition.getTrigger().getClass() == triggerClass) {
								PullRequestTrigger trigger = (PullRequestTrigger) transition.getTrigger();
								if (trigger.getBranches() == null || PatternSet.parse(trigger.getBranches()).matches(matcher, request.getTargetBranch())) {
									IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), true, false, false, true, false);
									List<IssueCriteria> criterias = new ArrayList<>();
									
									List<IssueCriteria> fromStateCriterias = new ArrayList<>();
									for (String fromState: transition.getFromStates()) 
										fromStateCriterias.add(new StateCriteria(fromState));
									
									criterias.add(IssueCriteria.or(fromStateCriterias));
									criterias.add(query.getCriteria());
									query = new IssueQuery(IssueCriteria.and(criterias), new ArrayList<>());
									PullRequest.push(request);
									try {
										for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
											issue.removeFields(transition.getRemoveFields());
											changeState(issue, transition.getToState(), new HashMap<>(), null);
										}
									} finally {
										PullRequest.pop();
									}
								}
							}
						}
					}
				});
			}
		});
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event.getChange().getData() instanceof PullRequestMergeData) {
			for (Long issueNumber: event.getRequest().getFixedIssueNumbers()) {
				Issue issue = issueManager.find(event.getProject(), issueNumber);
				if (issue != null) {
					IssueChange change = new IssueChange();
					change.setDate(event.getDate());
					change.setUser(event.getUser());
					change.setIssue(issue);
					change.setData(new IssuePullRequestMergedData(event.getRequest()));
					save(change);
				}
			}
			on(event.getRequest(), MergePullRequest.class);
		} else if (event.getChange().getData() instanceof PullRequestDiscardData) {
			for (Long issueNumber: event.getRequest().getFixedIssueNumbers()) {
				Issue issue = issueManager.find(event.getProject(), issueNumber);
				if (issue != null) {
					IssueChange change = new IssueChange();
					change.setDate(event.getDate());
					change.setUser(event.getUser());
					change.setIssue(issue);
					change.setData(new IssuePullRequestDiscardedData(event.getRequest()));
					save(change);
				}
			}
			on(event.getRequest(), DiscardPullRequest.class);
		} else if (event.getChange().getData() instanceof PullRequestReopenData) {
			for (Long issueNumber: event.getRequest().getFixedIssueNumbers()) {
				Issue issue = issueManager.find(event.getProject(), issueNumber);
				if (issue != null) {
					IssueChange change = new IssueChange();
					change.setDate(event.getDate());
					change.setUser(event.getUser());
					change.setIssue(issue);
					change.setData(new IssuePullRequestReopenedData(event.getRequest()));
					save(change);
				}
			}
			on(event.getRequest(), OpenPullRequest.class);
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		for (Long issueNumber: event.getRequest().getFixedIssueNumbers()) {
			Issue issue = issueManager.find(event.getProject(), issueNumber);
			if (issue != null) {
				IssueChange change = new IssueChange();
				change.setDate(event.getDate());
				change.setUser(event.getUser());
				change.setIssue(issue);
				change.setData(new IssuePullRequestOpenedData(event.getRequest()));
				save(change);
			}
		}
		on(event.getRequest(), OpenPullRequest.class);
	}
	
	@Transactional
	@Listen
	public void on(RefUpdated event) {
		if (!event.getNewCommitId().equals(ObjectId.zeroId()) 
				&& GitUtils.ref2branch(event.getRefName()) != null) {
			for (Long issueNumber: event.getCommit().getFixedIssueNumbers()) {
				Issue issue = issueManager.find(event.getProject(), issueNumber);
				if (issue != null) {
					IssueChange change = new IssueChange();
					change.setIssue(issue);
					change.setDate(new Date());
					change.setData(new IssueCommittedData(event.getCommit().getCommitId().name()));
					save(change);
				}
			}

			Long projectId = event.getCommit().getProject().getId();
			ObjectId commitId = event.getCommit().getCommitId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					transactionManager.runAsync(new Runnable() {

						@Override
						public void run() {
							SecurityUtils.bindAsSystem();
							Project project = projectManager.load(projectId);
							ProjectAwareCommit commit = new ProjectAwareCommit(project, commitId);
							for (TransitionSpec transition: project.getIssueSetting().getTransitionSpecs(true)) {
								if (transition.getTrigger() instanceof BranchUpdateTrigger) {
									BranchUpdateTrigger trigger = (BranchUpdateTrigger) transition.getTrigger();
									String branches = trigger.getBranches();
									if (branches == null || project.isCommitOnBranches(commitId, branches)) {
										IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), 
												true, false, false, false, true);
										List<IssueCriteria> criterias = new ArrayList<>();
										
										List<IssueCriteria> fromStateCriterias = new ArrayList<>();
										for (String fromState: transition.getFromStates()) 
											fromStateCriterias.add(new StateCriteria(fromState));
										
										criterias.add(IssueCriteria.or(fromStateCriterias));
										criterias.add(query.getCriteria());
										query = new IssueQuery(IssueCriteria.and(criterias), new ArrayList<>());
										ProjectAwareCommit.push(commit);
										try {
											for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
												issue.removeFields(transition.getRemoveFields());
												changeState(issue, transition.getToState(), new HashMap<>(), null);
											}
										} finally {
											ProjectAwareCommit.pop();
										}
									}
								}
							}
                            for (Build build: buildManager.query(project, commitId)) {
                            	for (TransitionSpec transition: project.getIssueSetting().getTransitionSpecs(true)) 
                            		checkBuild(transition, build);
                            }
						}
					});
				}
			});
		}
	}

}
