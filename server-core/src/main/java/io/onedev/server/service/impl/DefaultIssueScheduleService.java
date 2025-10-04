package io.onedev.server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.base.Preconditions;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueSchedule;
import io.onedev.server.model.Iteration;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueScheduleService;

@Singleton
public class DefaultIssueScheduleService extends BaseEntityService<IssueSchedule>
		implements IssueScheduleService {

 	@Transactional
 	@Override
 	public void syncIterations(Issue issue, Collection<Iteration> iterations) {
    	for (Iterator<IssueSchedule> it = issue.getSchedules().iterator(); it.hasNext();) {
    		IssueSchedule schedule = it.next();
    		if (!iterations.contains(schedule.getIteration())) {
    			dao.remove(schedule);
    			it.remove();
    		}
    	}
    	for (Iteration iteration: iterations) {
    		if (!issue.getIterations().contains(iteration)) {
    	    	IssueSchedule schedule = new IssueSchedule();
    	    	schedule.setIssue(issue);
    	    	schedule.setIteration(iteration);
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

	@Sessional
	@Override
	public void populateSchedules(Collection<Issue> issues) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueSchedule> query = builder.createQuery(IssueSchedule.class);

		Root<IssueSchedule> root = query.from(IssueSchedule.class);
		query.select(root);

		query.where(root.get(IssueSchedule.PROP_ISSUE).in(issues));

		for (Issue issue: issues)
			issue.setSchedules(new ArrayList<>());

		for (IssueSchedule schedule: getSession().createQuery(query).getResultList())
			schedule.getIssue().getSchedules().add(schedule);
	}
	
}
