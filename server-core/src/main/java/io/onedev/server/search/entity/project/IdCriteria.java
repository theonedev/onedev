package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class IdCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final long value;
	
	private final int operator;
	
	public IdCriteria(long value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		if (operator == ProjectQueryLexer.Is) 
			return builder.equal(from.get(Project.PROP_ID), value);
		else
			return builder.not(builder.equal(from.get(Project.PROP_ID), value));
	}

	@Override
	public boolean matches(Project project) {
		if (operator == ProjectQueryLexer.Is)
			return project.getId().equals(value);
		else
			return !project.getId().equals(value);
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_ID) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(String.valueOf(value));
	}

}
