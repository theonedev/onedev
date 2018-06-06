package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.util.lang.Objects;

import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.issue.IssueChanged;
import io.onedev.server.manager.IssueChangeManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.model.support.issue.IssueField;
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
	public void changeTitle(Issue issue, String prevTitle) {
		if (!prevTitle.equals(issue.getTitle())) {
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed title");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
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
	public void changeDescription(Issue issue, String prevDescription) {
		if (!Objects.equal(prevDescription, issue.getDescription())) {
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed description");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
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
	public void changeMilestone(Issue issue, String prevMilestone) {
		if (!Objects.equal(prevMilestone, issue.getMilestoneName())) {
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed milestone");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new MilestoneChangeData(prevMilestone, issue.getMilestoneName()));
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
	@Transactional
	@Override
	public void changeFields(Issue issue, Serializable fieldBean, Map<String, IssueField> prevFields, 
			Collection<String> fieldNames) {
		issueFieldUnaryManager.writeFields(issue, fieldBean, fieldNames);

		if (!prevFields.equals(issue.getEffectiveFields())) {
			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed custom fields");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
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
	public void changeState(Issue issue, Serializable fieldBean, @Nullable String commentContent,
			String prevState, Map<String, IssueField> prevFields, Collection<String> fieldNames) {
		if (!prevState.equals(issue.getState())) {
			issueFieldUnaryManager.writeFields(issue, fieldBean, fieldNames);

			LastActivity lastActivity = new LastActivity();
			lastActivity.setAction("changed state to \"" + issue.getState() + "\"");
			lastActivity.setDate(new Date());
			lastActivity.setUser(SecurityUtils.getUser());
			issueManager.save(issue);
			
			IssueChange change = new IssueChange();
			change.setIssue(issue);
			change.setDate(new Date());
			change.setUser(SecurityUtils.getUser());
			change.setData(new StateChangeData(issue.getState(), prevState, prevFields, issue.getEffectiveFields(), commentContent));
			
			save(change);
			
			listenerRegistry.post(new IssueChanged(change));
		}
	}
	
}
