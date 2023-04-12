package io.onedev.server.search.entity.project;

import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastEventDate;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;
import java.util.Date;

public class CommitDateCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public CommitDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<Date> attribute = ProjectQuery.getPath(from, Project.PROP_LAST_EVENT_DATE + "." + ProjectLastEventDate.PROP_COMMIT);
		if (operator == ProjectQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Project project) {
		if (project.getLastEventDate().getCommit() != null) {
			if (operator == ProjectQueryLexer.IsUntil)
				return project.getLastEventDate().getCommit().before(date);
			else
				return project.getLastEventDate().getCommit().after(date);
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_LAST_COMMIT_DATE) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
