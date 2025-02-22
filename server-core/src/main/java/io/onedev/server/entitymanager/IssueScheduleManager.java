package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.persistence.dao.EntityManager;

public interface IssueScheduleManager extends EntityManager<IssueSchedule> {
	
 	void syncIterations(Issue issue, Collection<Iteration> iterations);

    void create(IssueSchedule schedule);

    void populateSchedules(Collection<Issue> issues);
	
}