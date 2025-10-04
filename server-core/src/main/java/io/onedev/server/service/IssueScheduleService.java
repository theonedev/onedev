package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;

public interface IssueScheduleService extends EntityService<IssueSchedule> {
	
 	void syncIterations(Issue issue, Collection<Iteration> iterations);

    void create(IssueSchedule schedule);

    void populateSchedules(Collection<Issue> issues);
	
}