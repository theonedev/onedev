package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;

import com.google.common.base.Optional;

import io.onedev.launcher.loader.Listen;
import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueCommitted;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.event.pullrequest.PullRequestOpened;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueDescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.model.support.issue.changedata.IssueMilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.model.support.issue.changedata.IssueTitleChangeData;
import io.onedev.server.model.support.issue.workflow.TransitionSpec;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.BuildSuccessfulTrigger;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.CommitTrigger;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.DiscardPullRequest;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.MergePullRequest;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.OpenPullRequest;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.PullRequestTrigger;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestMergeData;
import io.onedev.server.persistence.UnitOfWork;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.IssueField;

@Singleton
public class DefaultIssueChangeManager extends AbstractEntityManager<IssueChange>
		implements IssueChangeManager {

	private final IssueManager issueManager;
	
	private final IssueFieldUnaryManager issueFieldUnaryManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final UnitOfWork unitOfWork;
	
	private final ExecutorService executorService;
	
	private final BuildManager buildManager;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, IssueManager issueManager, UnitOfWork unitOfWork,
			IssueFieldUnaryManager issueFieldUnaryManager, ListenerRegistry listenerRegistry, 
			ExecutorService executorService, BuildManager buildManager) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldUnaryManager = issueFieldUnaryManager;
		this.listenerRegistry = listenerRegistry;
		this.unitOfWork = unitOfWork;
		this.executorService = executorService;
		this.buildManager = buildManager;
	}

	@Transactional
	@Override
	public void save(IssueChange change) {
		dao.persist(change);
		listenerRegistry.post(new IssueChangeEvent(change));
	}
	
	@Transactional
	@Override
	public void changeTitle(Issue issue, String title, @Nullable User user) {
		String prevTitle = issue.getTitle();
		if (!title.equals(prevTitle)) {
			issue.setTitle(title);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(user);
			change.setData(new IssueTitleChangeData(prevTitle, issue.getTitle()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeDescription(Issue issue, @Nullable String description, @Nullable User user) {
		String prevDescription = issue.getDescription();
		if (!Objects.equal(description, prevDescription)) {
			issue.setDescription(description);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(user);
			change.setData(new IssueDescriptionChangeData(prevDescription, issue.getDescription()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeMilestone(Issue issue, @Nullable Milestone milestone, @Nullable User user) {
		Milestone prevMilestone = issue.getMilestone();
		if (!Objects.equal(prevMilestone, milestone)) {
			issue.setMilestone(milestone);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(user);
			change.setData(new IssueMilestoneChangeData(prevMilestone, issue.getMilestone()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Map<String, Object> fieldValues, @Nullable User user) {
		Map<String, IssueField> prevFields = issue.getFields(); 
		issue.setFieldValues(fieldValues);
		if (!prevFields.equals(issue.getFields())) {
			issueFieldUnaryManager.saveFields(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(user);
			change.setData(new IssueFieldChangeData(prevFields, issue.getFields()));
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void changeState(Issue issue, String state, Map<String, Object> fieldValues, @Nullable String comment, @Nullable User user) {
		String prevState = issue.getState();
		if (!prevState.equals(state)) {
			Map<String, IssueField> prevFields = issue.getFields();
			issue.setState(state);

			issue.setFieldValues(fieldValues);
			
			issueFieldUnaryManager.saveFields(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(user);
			change.setData(new IssueStateChangeData(prevState, issue.getState(), prevFields, issue.getFields(), comment));
			
			save(change);
		}
	}
	
	@Transactional
	@Override
	public void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, 
			@Nullable Optional<Milestone> milestone, Map<String, Object> fieldValues, 
			@Nullable String comment, @Nullable User user) {
		while (issues.hasNext()) {
			Issue issue = issues.next();
			String prevState = issue.getState();
			Milestone prevMilestone = issue.getMilestone();
			Map<String, IssueField> prevFields = issue.getFields();
			if (state != null)
				issue.setState(state);
			if (milestone != null)
				issue.setMilestone(milestone.orNull());
			
			issue.setFieldValues(fieldValues);
			issueFieldUnaryManager.saveFields(issue);

			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(user);
			change.setData(new IssueBatchUpdateData(prevState, issue.getState(), prevMilestone, issue.getMilestone(), prevFields, issue.getFields(), comment));
			
			save(change);
		}
	}
	
	@Transactional
	@Listen
	public void on(BuildEvent event) {
		for (TransitionSpec transition: event.getBuild().getConfiguration().getProject().getIssueWorkflow().getTransitionSpecs()) {
			if (transition.getTrigger() instanceof BuildSuccessfulTrigger) {
				BuildSuccessfulTrigger trigger = (BuildSuccessfulTrigger) transition.getTrigger();
				if (trigger.getConfiguration().equals(event.getBuild().getConfiguration().getName()) 
						&& event.getBuild().getStatus() == Build.Status.SUCCESS) {
					Long buildId = event.getBuild().getId();
					
					/* 
					 * Below logic is carefully written to make sure database connection is not occupied 
					 * while waiting to avoid deadlocks
					 */
					executorService.execute(new Runnable() {

						@Override
						public void run() {
							while (true) {
								if (unitOfWork.call(new Callable<Boolean>() {

									@Override
									public Boolean call() {
										Build build = buildManager.load(buildId);
										Collection<Long> fixedIssueNumbers = build.getFixedIssueNumbers();
										if (fixedIssueNumbers != null) {
											for (Long issueNumber: fixedIssueNumbers) {
												Issue issue = issueManager.find(build.getConfiguration().getProject(), issueNumber);
												if (issue != null && transition.getFromStates().contains(issue.getState())) { 
													issue.removeFields(transition.getRemoveFields());
													changeState(issue, transition.getToState(), new HashMap<>(), null, null);
												}
											}
											return true;
										} else {
											return false;
										}
									}
									
								})) {
									break;
								} else {
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
									}
								}
							}
						}
						
					});
				}
			}
		}
	}
	
	private void on(PullRequest request, Class<? extends PullRequestTrigger> triggerClass) {
		for (TransitionSpec transition: request.getTargetProject().getIssueWorkflow().getTransitionSpecs()) {
			if (transition.getTrigger().getClass() == triggerClass) {
				PullRequestTrigger trigger = (PullRequestTrigger) transition.getTrigger();
				if (trigger.getBranch().equals(request.getTargetBranch())) {
					for (Issue issue: request.getFixedIssues()) {
						if (transition.getFromStates().contains(issue.getState())) {
							issue.removeFields(transition.getRemoveFields());
							changeState(issue, transition.getToState(), new HashMap<>(), null, null);
						}
					}
				}
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event.getChange().getData() instanceof PullRequestMergeData)
			on(event.getRequest(), MergePullRequest.class);
		else if (event.getChange().getData() instanceof PullRequestDiscardData)
			on(event.getRequest(), DiscardPullRequest.class);
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		on(event.getRequest(), OpenPullRequest.class);
	}
	
	@Transactional
	@Listen
	public void on(IssueCommitted event) {
		Issue issue = event.getIssue();
		for (TransitionSpec transition: issue.getProject().getIssueWorkflow().getTransitionSpecs()) {
			if (transition.getTrigger() instanceof CommitTrigger && transition.getFromStates().contains(issue.getState())) {
				issue.removeFields(transition.getRemoveFields());
				changeState(issue, transition.getToState(), new HashMap<>(), null, null);
			}
		}
	}

}
