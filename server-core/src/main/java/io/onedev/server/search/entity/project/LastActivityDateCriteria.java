package io.onedev.server.search.entity.project;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastEventDate;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public class LastActivityDateCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public LastActivityDateCriteria(String value, int operator) {
		date = EntityQuery.getDateValue(value);
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<Date> attribute = ProjectQuery.getPath(from, Project.PROP_LAST_EVENT_DATE + "." + ProjectLastEventDate.PROP_ACTIVITY);
		if (operator == ProjectQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(Project project) {
		if (operator == ProjectQueryLexer.IsUntil)
			return project.getLastEventDate().getActivity().before(date);
		else
			return project.getLastEventDate().getActivity().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_LAST_ACTIVITY_DATE) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
