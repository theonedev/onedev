package io.onedev.server.manager.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;

import com.google.common.base.Optional;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.issue.IssueActionEvent;
import io.onedev.server.manager.IssueActionManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.changedata.BatchUpdateData;
import io.onedev.server.model.support.issue.changedata.FieldChangeData;
import io.onedev.server.model.support.issue.changedata.MilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.StateChangeData;
import io.onedev.server.model.support.issue.changedata.TitleChangeData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultIssueActionManager extends AbstractEntityManager<IssueAction>
		implements IssueActionManager {

	private final IssueManager issueManager;
	
	private final IssueFieldUnaryManager issueFieldUnaryManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueActionManager(Dao dao, IssueManager issueManager,  
			IssueFieldUnaryManager issueFieldUnaryManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.issueManager = issueManager;
		this.issueFieldUnaryManager = issueFieldUnaryManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Transactional
	@Override
	public void changeTitle(Issue issue, String title) {
		String prevTitle = issue.getTitle();
		if (!title.equals(prevTitle)) {
			issue.setTitle(title);
			LastActivity lastActivity = new LastActivity();
			lastActivity.setDescription("changed title");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueAction change = new IssueAction();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new TitleChangeData(prevTitle, issue.getTitle()));
			save(change);
			
			listenerRegistry.post(new IssueActionEvent(change));
		}
	}
	
	@Transactional
	@Override
	public void changeMilestone(Issue issue, @Nullable Milestone milestone) {
		Milestone prevMilestone = issue.getMilestone();
		if (!Objects.equal(prevMilestone, milestone)) {
			issue.setMilestone(milestone);
			LastActivity lastActivity = new LastActivity();
			lastActivity.setDescription("changed milestone");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueAction change = new IssueAction();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new MilestoneChangeData(prevMilestone, issue.getMilestone()));
			save(change);
			
			listenerRegistry.post(new IssueActionEvent(change));
		}
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Map<String, Object> fieldValues) {
		Map<String, IssueField> prevFields = issue.getFields(); 
		issue.setFieldValues(fieldValues);
		if (!prevFields.equals(issue.getFields())) {
			issueFieldUnaryManager.saveFields(issue);
			
			LastActivity lastActivity = new LastActivity();
			lastActivity.setDescription("changed fields");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueAction change = new IssueAction();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new FieldChangeData(prevFields, issue.getFields()));
			save(change);
			
			listenerRegistry.post(new IssueActionEvent(change));
		}
	}
	
	@Transactional
	@Override
	public void changeState(Issue issue, String state, Map<String, Object> fieldValues, @Nullable String comment) {
		String prevState = issue.getState();
		if (!prevState.equals(state)) {
			Map<String, IssueField> prevFields = issue.getFields();
			issue.setState(state);

			issue.setFieldValues(fieldValues);
			
			issueFieldUnaryManager.saveFields(issue);

			LastActivity lastActivity = new LastActivity();
			lastActivity.setDescription("changed state to \"" + issue.getState() + "\"");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueAction change = new IssueAction();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new StateChangeData(prevState, issue.getState(), prevFields, issue.getFields(), comment));
			
			save(change);
			
			listenerRegistry.post(new IssueActionEvent(change));
		}
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
			Map<String, IssueField> prevFields = issue.getFields();
			if (state != null)
				issue.setState(state);
			if (milestone != null)
				issue.setMilestone(milestone.orNull());
			
			issue.setFieldValues(fieldValues);
			issueFieldUnaryManager.saveFields(issue);

			LastActivity lastActivity = new LastActivity();
			lastActivity.setDescription("batch edited issue");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueAction change = new IssueAction();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new BatchUpdateData(prevState, issue.getState(), prevMilestone, issue.getMilestone(), prevFields, issue.getFields(), comment));
			
			save(change);
			
			listenerRegistry.post(new IssueActionEvent(change));
		}
	}
	
}
