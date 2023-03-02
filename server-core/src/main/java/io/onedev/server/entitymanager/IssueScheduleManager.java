package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Milestone;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueScheduleManager extends EntityManager<IssueSchedule> {
	
 	void syncMilestones(Issue issue, Collection<Milestone> milestones);

    void create(IssueSchedule schedule);
}