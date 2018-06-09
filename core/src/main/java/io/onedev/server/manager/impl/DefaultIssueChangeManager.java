package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;

import com.google.common.base.Optional;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.issue.IssueChanged;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.changedata.BatchChangeData;
import io.onedev.server.model.support.issue.changedata.DescriptionChangeData;
import io.onedev.server.model.support.issue.changedata.FieldChangeData;
import io.onedev.server.model.support.issue.changedata.MilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.StateChangeData;
import io.onedev.server.model.support.issue.changedata.TitleChangeData;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultIssueChangeManager extends AbstractEntityManager<IssueChange>
		implements IssueChangeManager {

	private final IssueManager issueManager;
	
	private final IssueFieldUnaryManager issueFieldUnaryManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultIssueChangeManager(Dao dao, IssueManager issueManager,  
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
			lastActivity.setAction("changed title");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new TitleChangeData(prevTitle, issue.getTitle()));
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
	@Transactional
	@Override
	public void changeDescription(Issue issue, @Nullable String description) {
		String prevDescription = issue.getDescription();
		if (!Objects.equal(prevDescription, description)) {
			issue.setDescription(description);
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed description");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new DescriptionChangeData(prevDescription, issue.getDescription()));
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}

	@Transactional
	@Override
	public void changeMilestone(Issue issue, @Nullable Milestone milestone) {
		Milestone prevMilestone = issue.getMilestone();
		if (!Objects.equal(prevMilestone, milestone)) {
			issue.setMilestone(milestone);
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed milestone");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new MilestoneChangeData(prevMilestone, issue.getMilestone()));
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Serializable fieldBean, Collection<String> fieldNames) {
		Map<String, IssueField> prevFields = issue.getEffectiveFields(); 
		issueFieldUnaryManager.writeFields(issue, fieldBean, fieldNames);

		if (!prevFields.equals(issue.getEffectiveFields())) {
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed fields");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new FieldChangeData(prevFields, issue.getEffectiveFields()));
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
	@Transactional
	@Override
	public void changeState(Issue issue, String state, Serializable fieldBean, 
			Collection<String> fieldNames, @Nullable String comment) {
		String prevState = issue.getState();
		if (!prevState.equals(state)) {
			Map<String, IssueField> prevFields = issue.getEffectiveFields();
			issue.setState(state);
			issueFieldUnaryManager.writeFields(issue, fieldBean, fieldNames);

			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed state to \"" + issue.getState() + "\"");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new StateChangeData(prevState, issue.getState(), prevFields, issue.getEffectiveFields(), comment));
			
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
	@Transactional
	@Override
	public void batchUpdate(Iterator<? extends Issue> issues, @Nullable String state, @Nullable Optional<Milestone> milestone, 
			Serializable fieldBean, Set<String> fieldNames, @Nullable String comment) {
		while (issues.hasNext()) {
			Issue issue = issues.next();
			String prevState = issue.getState();
			Milestone prevMilestone = issue.getMilestone();
			Map<String, IssueField> prevFields = issue.getEffectiveFields();
			if (state != null)
				issue.setState(state);
			if (milestone != null)
				issue.setMilestone(milestone.orNull());
			
			issueFieldUnaryManager.writeFields(issue, fieldBean, fieldNames);

			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("batch edited issue");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issue.setLastActivity(lastActivity);
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new BatchChangeData(prevState, issue.getState(), prevMilestone, issue.getMilestone(), prevFields, issue.getEffectiveFields(), comment));
			
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
}
