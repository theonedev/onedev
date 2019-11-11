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
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.issue.IssueChangeEvent;
import io.onedev.server.event.issue.IssueOpened;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
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
	
	@Inject
	public DefaultMilestoneManager(Dao dao, SettingManager settingManager) {
		super(dao);
		this.settingManager = settingManager;
	}

	private IssueSetting getIssueSetting() {
		return settingManager.getIssueSetting();
	}
	
	@Transactional
	@Override
	public void delete(Milestone milestone, @Nullable Milestone moveIssuesToMilestone) {
		if (moveIssuesToMilestone != null) {
			Query<?> query = getSession().createQuery("update Issue set milestone=:newMilestone where milestone=:milestone");
			query.setParameter("milestone", milestone);
			query.setParameter("newMilestone", moveIssuesToMilestone);
			query.executeUpdate();
			moveIssuesToMilestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()+moveIssuesToMilestone.getNumOfClosedIssues());
			moveIssuesToMilestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()+moveIssuesToMilestone.getNumOfOpenIssues());
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
				StateSpec state = getIssueSetting().getStateSpec(issue.getState());
				Preconditions.checkNotNull(state);
				if (state.getCategory() == StateSpec.Category.CLOSED) 
					milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()-1);
				else
					milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()-1);
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
			if (oldStateSpec.getCategory() != newStateSpec.getCategory()) {
				if (oldStateSpec.getCategory() == StateSpec.Category.CLOSED) {
					milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()-1);
					milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()+1);
				} else {
					milestone.setNumOfClosedIssues(milestone.getNumOfClosedIssues()+1);
					milestone.setNumOfOpenIssues(milestone.getNumOfOpenIssues()-1);
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
		if (stateSpec.getCategory() == StateSpec.Category.CLOSED) {
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
	
}
