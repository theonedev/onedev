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
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueCommitted;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.issue.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.issue.transitiontrigger.CommitTrigger;
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
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueCommittedData;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
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
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultIssueChangeManager extends AbstractEntityManager<IssueChange>
		implements IssueChangeManager {

	private final IssueManager issueManager;
	
	private final IssueFieldManager issueFieldManager;
	
	private final TransactionManager transactionManager;
	
	private final BuildManager buildManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, TransactionManager transactionManager, 
			IssueManager issueManager,  IssueFieldManager issueFieldManager,
			BuildManager buildManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldManager = issueFieldManager;
		this.transactionManager = transactionManager;
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

	@Transactional
	@Override
	public void changeDescription(Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equal(description, prevDescription)) {
			issue.setDescription(description);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new IssueDescriptionChangeData(prevDescription, issue.getDescription()));
			save(change);
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
						Build build = buildManager.load(buildId);
						Project project = build.getProject();
						for (TransitionSpec transition: project.getIssueSetting().getTransitionSpecs(true)) {
							if (transition.getTrigger() instanceof BuildSuccessfulTrigger) {
								BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) transition.getTrigger();
								String branches = trigger.getBranches();
								ObjectId commitId = ObjectId.fromString(build.getCommitHash());
								if ((trigger.getJobNames() == null || PatternSet.fromString(trigger.getJobNames()).matches(new StringMatcher(), build.getJobName())) 
										&& build.getStatus() == Build.Status.SUCCESSFUL
										&& (branches == null || project.isCommitOnBranches(commitId, branches))) {
									IssueQuery query = IssueQuery.parse(project, trigger.getIssueQuery(), true);
									List<IssueCriteria> criterias = new ArrayList<>();
									for (String fromState: transition.getFromStates()) 
										criterias.add(new StateCriteria(fromState));
									criterias.add(query.getCriteria());
									query = new IssueQuery(IssueCriteria.of(criterias), new ArrayList<>());
									Build.push(build);
									User.push(null); // do not support various 'is me' criterias
									try {
										for (Issue issue: issueManager.query(project, query, 0, Integer.MAX_VALUE)) {
											issue.removeFields(transition.getRemoveFields());
											changeState(issue, transition.getToState(), new HashMap<>(), null);
										}
									} finally {
										Build.pop();
										User.pop();
									}
								}
							}
						}
					}
				});
			}
		});
	}
	
	private void on(PullRequest request, Class<? extends PullRequestTrigger> triggerClass) {
		for (TransitionSpec transition: request.getTargetProject().getIssueSetting().getTransitionSpecs(true)) {
			if (transition.getTrigger().getClass() == triggerClass) {
				PullRequestTrigger trigger = (PullRequestTrigger) transition.getTrigger();
				if (trigger.getBranch().equals(request.getTargetBranch())) {
					for (Issue issue: request.getFixedIssues()) {
						if (transition.getFromStates().contains(issue.getState())) {
							issue.removeFields(transition.getRemoveFields());
							changeState(issue, transition.getToState(), new HashMap<>(), null);
						}
					}
				}
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event.getChange().getData() instanceof PullRequestMergeData) {
			for (Issue issue: event.getRequest().getFixedIssues()) {
				IssueChange change = new IssueChange();
				change.setDate(event.getDate());
				change.setUser(event.getUser());
				change.setIssue(issue);
				change.setData(new IssuePullRequestMergedData(event.getRequest()));
				save(change);
			}
			on(event.getRequest(), MergePullRequest.class);
		} else if (event.getChange().getData() instanceof PullRequestDiscardData) {
			for (Issue issue: event.getRequest().getFixedIssues()) {
				IssueChange change = new IssueChange();
				change.setDate(event.getDate());
				change.setUser(event.getUser());
				change.setIssue(issue);
				change.setData(new IssuePullRequestDiscardedData(event.getRequest()));
				save(change);
			}
			on(event.getRequest(), DiscardPullRequest.class);
		} else if (event.getChange().getData() instanceof PullRequestReopenData) {
			for (Issue issue: event.getRequest().getFixedIssues()) {
				IssueChange change = new IssueChange();
				change.setDate(event.getDate());
				change.setUser(event.getUser());
				change.setIssue(issue);
				change.setData(new IssuePullRequestReopenedData(event.getRequest()));
				save(change);
			}
			on(event.getRequest(), OpenPullRequest.class);
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		for (Issue issue: event.getRequest().getFixedIssues()) {
			IssueChange change = new IssueChange();
			change.setDate(event.getDate());
			change.setUser(event.getUser());
			change.setIssue(issue);
			change.setData(new IssuePullRequestOpenedData(event.getRequest()));
			save(change);
		}
		on(event.getRequest(), OpenPullRequest.class);
	}
	
	@Transactional
	@Listen
	public void on(IssueCommitted event) {
		Issue issue = event.getIssue();
		
		IssueChange change = new IssueChange();
		change.setIssue(issue);
		change.setDate(new Date());
		change.setData(new IssueCommittedData(event.getFixCommits()));
		save(change);
		
		for (TransitionSpec transition: issue.getProject().getIssueSetting().getTransitionSpecs(true)) {
			if (transition.getTrigger() instanceof CommitTrigger && transition.getFromStates().contains(issue.getState())) {
				CommitTrigger commitTrigger = (CommitTrigger) transition.getTrigger();
				boolean applicable;
				if (commitTrigger.getBranches() == null) {
					applicable = true;
				} else {
					for (ObjectId commitId: event.getFixCommits()) {
						if (issue.getProject().isCommitOnBranches(commitId, commitTrigger.getBranches())) {
							applicable = true;
							break;
						}
					}
					applicable = false;
				}
				if (applicable) {
					issue.removeFields(transition.getRemoveFields());
					changeState(issue, transition.getToState(), new HashMap<>(), null);
				}
			}
		}
	}

}
