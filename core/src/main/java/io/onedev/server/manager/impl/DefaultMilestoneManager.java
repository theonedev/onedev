package io.onedev.server.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Preconditions;

import io.onedev.launcher.loader.Listen;
import io.onedev.server.event.issue.IssueChanged;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.changedata.MilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.StateChangeData;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.persistence.dao.EntityRemoved;
import io.onedev.utils.StringUtils;

@Singleton
public class DefaultMilestoneManager extends AbstractEntityManager<Milestone> implements MilestoneManager {

	private final IssueManager issueManager;
	
	@Inject
	public DefaultMilestoneManager(Dao dao, IssueManager issueManager) {
		super(dao);
		this.issueManager = issueManager;
	}

	@Transactional
	@Override
	public void delete(Milestone milestone, @Nullable Milestone moveIssuesToMilestone) {
    	Query<?> query = getSession().createQuery("update IssueBoard set milestone=null where milestone=:milestone");
    	query.setParameter("milestone", milestone);
    	query.executeUpdate();
		
		if (moveIssuesToMilestone != null) {
			query = getSession().createQuery("update Issue set milestone=:newMilestone where milestone=:milestone");
			query.setParameter("milestone", milestone);
			query.setParameter("newMilestone", moveIssuesToMilestone);
			query.executeUpdate();
			moveIssuesToMilestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()+moveIssuesToMilestone.getNumOfClosedIssues());
			moveIssuesToMilestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()+moveIssuesToMilestone.getNumOfOpenIssues());
			save(moveIssuesToMilestone);
		} else {
			query = getSession().createQuery("update Issue set milestone=null where milestone=:milestone");
			query.setParameter("milestone", milestone);
			query.executeUpdate();
		}
		super.delete(milestone);
	}
	
	@Override
	public void updateIssueCount(Milestone milestone, StateSpec.Category category) {
		IssueCriteria criteria = milestone.getProject().getIssueWorkflow().getStatesCriteria(category);
		if (criteria != null) {
			if (category == StateSpec.Category.CLOSED)
				milestone.setNumOfClosedIssues(issueManager.count(milestone.getProject(), criteria));
			else
				milestone.setNumOfOpenIssues(issueManager.count(milestone.getProject(), criteria));
		} else {
			if (category == StateSpec.Category.CLOSED)
				milestone.setNumOfClosedIssues(0);
			else
				milestone.setNumOfOpenIssues(0);
		}
		save(milestone);
	}

	@Sessional
	@Override
	public Milestone find(Project project, String name) {
		EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("name", name));
		return find(criteria);
	}

	@Transactional
	@Override
	public void close(Milestone milestone, @Nullable Milestone moveOpenIssuesToMilestone) {
		List<String> criterias = new ArrayList<>();
		for (StateSpec state: milestone.getProject().getIssueWorkflow().getStateSpecs()) {
			if (state.getCategory() == StateSpec.Category.OPEN)
				criterias.add("state='" + state.getName() + "'");
		}
		if (!criterias.isEmpty()) {
			String stateClause = "(" + StringUtils.join(criterias, " or ") + ")";
			if (moveOpenIssuesToMilestone != null) {
				Query<?> query = getSession().createQuery("update Issue set milestone=:newMilestone where milestone=:milestone and " + stateClause);
				query.setParameter("milestone", milestone);
				query.setParameter("newMilestone", moveOpenIssuesToMilestone);
				query.executeUpdate();
				moveOpenIssuesToMilestone.setNumOfOpenIssues(moveOpenIssuesToMilestone.getNumOfOpenIssues()+milestone.getNumOfOpenIssues());
				milestone.setNumOfOpenIssues(0);
				save(moveOpenIssuesToMilestone);
			} else {
				Query<?> query = getSession().createQuery("update Issue set milestone=null where milestone=:milestone and " + stateClause);
				query.setParameter("milestone", milestone);
				query.executeUpdate();
				milestone.setNumOfOpenIssues(0);
			}
		}
		milestone.setUpdateDate(new Date());
		milestone.setClosed(true);
		save(milestone);
	}

	@Transactional
	@Listen
	public void on(IssueOpened event) {
		Issue issue = event.getIssue();
		Milestone milestone = issue.getMilestone();
		if (milestone != null) {
			StateSpec state = issue.getProject().getIssueWorkflow().getStateSpec(issue.getState());
			Preconditions.checkNotNull(state);
			if (state.getCategory() == StateSpec.Category.CLOSED)
				milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()+1);
			else
				milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()+1);
			save(milestone);
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			Milestone milestone = issue.getMilestone();
			if (milestone != null) {
				StateSpec state = issue.getProject().getIssueWorkflow().getStateSpec(issue.getState());
				Preconditions.checkNotNull(state);
				if (state.getCategory() == StateSpec.Category.CLOSED) 
					milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()-1);
				else
					milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()-1);
				save(milestone);
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(IssueChanged event) {
		Issue issue = event.getIssue();
		Milestone milestone = issue.getMilestone();
		if (milestone != null && event.getChange().getData() instanceof StateChangeData) {
			StateChangeData data = (StateChangeData) event.getChange().getData();
			StateSpec prevState = issue.getProject().getIssueWorkflow().getStateSpec(data.getOldState());
			Preconditions.checkNotNull(prevState);
			StateSpec state = issue.getProject().getIssueWorkflow().getStateSpec(data.getNewState());
			Preconditions.checkNotNull(prevState);
			if (prevState.getCategory() != state.getCategory()) {
				if (prevState.getCategory() == StateSpec.Category.CLOSED) {
					milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()-1);
					milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()+1);
				} else {
					milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()+1);
					milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()-1);
				}
				save(milestone);
			}
		} else if (event.getChange().getData() instanceof MilestoneChangeData) {
			MilestoneChangeData data = (MilestoneChangeData) event.getChange().getData();
			Milestone oldMilestone = issue.getProject().getMilestone(data.getOldMilestone());
			Milestone newMilestone = issue.getProject().getMilestone(data.getNewMilestone());
			StateSpec state = issue.getProject().getIssueWorkflow().getStateSpec(issue.getState());
			Preconditions.checkNotNull(state);
			if (state.getCategory() == StateSpec.Category.CLOSED) {
				if (oldMilestone != null)
					oldMilestone.setNumOfClosedIssues(oldMilestone.getNumOfClosedIssues()-1);
				if (newMilestone != null)
					newMilestone.setNumOfClosedIssues(newMilestone.getNumOfClosedIssues()+1);
			} else {
				if (oldMilestone != null)
					oldMilestone.setNumOfOpenIssues(oldMilestone.getNumOfOpenIssues()-1);
				if (newMilestone != null)
					newMilestone.setNumOfOpenIssues(newMilestone.getNumOfOpenIssues()+1);
			}
			if (oldMilestone != null)
				save(oldMilestone);
			if (newMilestone != null)
				save(newMilestone);
		}
	}
}
