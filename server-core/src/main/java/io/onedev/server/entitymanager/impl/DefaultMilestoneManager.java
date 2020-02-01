package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.issue.StateSpec;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.changedata.IssueBatchUpdateData;
import io.onedev.server.model.support.issue.changedata.IssueMilestoneChangeData;
import io.onedev.server.model.support.issue.changedata.IssueStateChangeData;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultMilestoneManager extends AbstractEntityManager<Milestone> implements MilestoneManager {

	private final SettingManager settingManager;
	
	private final ProjectManager projectManager;
	
	@Inject
	public DefaultMilestoneManager(Dao dao, SettingManager settingManager, ProjectManager projectManager) {
		super(dao);
		this.settingManager = settingManager;
		this.projectManager = projectManager;
	}

	private GlobalIssueSetting getIssueSetting() {
		return settingManager.getIssueSetting();
	}
	
	@Sessional
	@Override
	public Milestone find(String milestoneFQN) {
		String projectName = StringUtils.substringBefore(milestoneFQN, ":");
		Project project = projectManager.find(projectName);
		if (project != null) 
			return find(project, StringUtils.substringAfter(milestoneFQN, ":"));
		else 
			return null;
	}
	
	@Transactional
	@Override
	public void delete(Milestone milestone, @Nullable Milestone moveIssuesToMilestone) {
		if (moveIssuesToMilestone != null) {
			Query<?> query = getSession().createQuery("update Issue set milestone=:newMilestone where milestone=:milestone");
			query.setParameter("milestone", milestone);
			query.setParameter("newMilestone", moveIssuesToMilestone);
			query.executeUpdate();
			moveIssuesToMilestone.setNumOfIssuesDone(milestone.getNumOfIssuesDone()+moveIssuesToMilestone.getNumOfIssuesDone());
			moveIssuesToMilestone.setNumOfIssuesTodo(milestone.getNumOfIssuesTodo()+moveIssuesToMilestone.getNumOfIssuesTodo());
			save(moveIssuesToMilestone);
		} else {
			Query<?> query = getSession().createQuery("update Issue set milestone=null where milestone=:milestone");
			query.setParameter("milestone", milestone);
			query.executeUpdate();
		}
		super.delete(milestone);
	}
	
	@Sessional
	@Override
	public Milestone find(Project project, String name) {
		EntityCriteria<Milestone> criteria = EntityCriteria.of(Milestone.class);
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("name", name));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void close(Milestone milestone, @Nullable Milestone moveOpenIssuesToMilestone) {
		List<String> criterias = new ArrayList<>();
		for (StateSpec state: getIssueSetting().getStateSpecs()) {
			if (!state.isDone())
				criterias.add("state='" + state.getName() + "'");
		}
		if (!criterias.isEmpty()) {
			String stateClause = "(" + StringUtils.join(criterias, " or ") + ")";
			if (moveOpenIssuesToMilestone != null) {
				Query<?> query = getSession().createQuery("update Issue set milestone=:newMilestone where milestone=:milestone and " + stateClause);
				query.setParameter("milestone", milestone);
				query.setParameter("newMilestone", moveOpenIssuesToMilestone);
				query.executeUpdate();
				moveOpenIssuesToMilestone.setNumOfIssuesTodo(moveOpenIssuesToMilestone.getNumOfIssuesTodo()+milestone.getNumOfIssuesTodo());
				milestone.setNumOfIssuesTodo(0);
				save(moveOpenIssuesToMilestone);
			} else {
				Query<?> query = getSession().createQuery("update Issue set milestone=null where milestone=:milestone and " + stateClause);
				query.setParameter("milestone", milestone);
				query.executeUpdate();
				milestone.setNumOfIssuesTodo(0);
			}
		}
		milestone.setClosed(true);
		save(milestone);
	}

	@Transactional
	@Listen
	public void on(IssueOpened event) {
		Issue issue = event.getIssue();
		Milestone milestone = issue.getMilestone();
		if (milestone != null) {
			StateSpec state = getIssueSetting().getStateSpec(issue.getState());
			Preconditions.checkNotNull(state);
			if (state.isDone())
				milestone.setNumOfIssuesDone(milestone.getNumOfIssuesDone()+1);
			else
				milestone.setNumOfIssuesTodo(milestone.getNumOfIssuesTodo()+1);
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
				StateSpec state = getIssueSetting().getStateSpec(issue.getState());
				Preconditions.checkNotNull(state);
				if (state.isDone()) 
					milestone.setNumOfIssuesDone(milestone.getNumOfIssuesDone()-1);
				else
					milestone.setNumOfIssuesTodo(milestone.getNumOfIssuesTodo()-1);
				save(milestone);
			}
		}
	}

	private void onStateChange(Project project, @Nullable String milestoneName, String oldState, String newState) {
		Milestone milestone = project.getMilestone(milestoneName);
		if (milestone != null) {
			StateSpec oldStateSpec = getIssueSetting().getStateSpec(oldState);
			Preconditions.checkNotNull(oldStateSpec);
			StateSpec newStateSpec = getIssueSetting().getStateSpec(newState);
			Preconditions.checkNotNull(oldStateSpec);
			if (oldStateSpec.isDone() != newStateSpec.isDone()) {
				if (oldStateSpec.isDone()) {
					milestone.setNumOfIssuesDone(milestone.getNumOfIssuesDone()-1);
					milestone.setNumOfIssuesTodo(milestone.getNumOfIssuesTodo()+1);
				} else {
					milestone.setNumOfIssuesDone(milestone.getNumOfIssuesDone()+1);
					milestone.setNumOfIssuesTodo(milestone.getNumOfIssuesTodo()-1);
				}
				save(milestone);
			}
		}
	}
	
	private void onMilestoneChange(Project project, String state, @Nullable String oldMilestoneName, @Nullable String newMilestoneName) {
		Milestone oldMilestone = project.getMilestone(oldMilestoneName);
		Milestone newMilestone = project.getMilestone(newMilestoneName);
		StateSpec stateSpec = getIssueSetting().getStateSpec(state);
		Preconditions.checkNotNull(stateSpec);
		if (stateSpec.isDone()) {
			if (oldMilestone != null)
				oldMilestone.setNumOfIssuesDone(oldMilestone.getNumOfIssuesDone()-1);
			if (newMilestone != null)
				newMilestone.setNumOfIssuesDone(newMilestone.getNumOfIssuesDone()+1);
		} else {
			if (oldMilestone != null)
				oldMilestone.setNumOfIssuesTodo(oldMilestone.getNumOfIssuesTodo()-1);
			if (newMilestone != null)
				newMilestone.setNumOfIssuesTodo(newMilestone.getNumOfIssuesTodo()+1);
		}
		if (oldMilestone != null)
			save(oldMilestone);
		if (newMilestone != null)
			save(newMilestone);
	}
	
	@Transactional
	@Listen
	public void on(IssueChangeEvent event) {
		Project project = event.getIssue().getProject();
		if (event.getChange().getData() instanceof IssueBatchUpdateData) {
			IssueBatchUpdateData data = (IssueBatchUpdateData) event.getChange().getData();
			onStateChange(project, data.getOldMilestone(), data.getOldState(), data.getNewState());
			onMilestoneChange(project, data.getNewState(), data.getOldMilestone(), data.getNewMilestone());
		} else if (event.getChange().getData() instanceof IssueStateChangeData) {
			IssueStateChangeData data = (IssueStateChangeData) event.getChange().getData();
			onStateChange(project, event.getIssue().getMilestoneName(), data.getOldState(), data.getNewState());
		} else if (event.getChange().getData() instanceof IssueMilestoneChangeData) {
			IssueMilestoneChangeData data = (IssueMilestoneChangeData) event.getChange().getData();
			onMilestoneChange(project, event.getIssue().getState(), data.getOldMilestone(), data.getNewMilestone());
		}
	}
	
	@Override
	public List<Milestone> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
