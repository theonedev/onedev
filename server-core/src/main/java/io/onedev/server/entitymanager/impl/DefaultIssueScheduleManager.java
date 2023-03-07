package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.IssueScheduleManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultIssueScheduleManager extends BaseEntityManager<IssueSchedule> 
		implements IssueScheduleManager {

	@Inject
	public DefaultIssueScheduleManager(Dao dao) {
		super(dao);
	}

 	@Transactional
 	@Override
 	public void syncMilestones(Issue issue, Collection<Milestone> milestones) {
    	for (Iterator<IssueSchedule> it = issue.getSchedules().iterator(); it.hasNext();) {
    		IssueSchedule schedule = it.next();
    		if (!milestones.contains(schedule.getMilestone())) {
    			dao.remove(schedule);
    			it.remove();
    		}
    	}
    	for (Milestone milestone: milestones) {
    		if (!issue.getMilestones().contains(milestone)) {
    	    	IssueSchedule schedule = new IssueSchedule();
    	    	schedule.setIssue(issue);
    	    	schedule.setMilestone(milestone);
    	    	issue.getSchedules().add(schedule);
    	    	dao.persist(schedule);
    		}
    	}
 	}

	 @Transactional
	@Override
	public void create(IssueSchedule schedule) {
		Preconditions.checkState(schedule.isNew());
		dao.persist(schedule);
	}

}
