package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.commons.utils.match.WildcardUtils;

public class ServiceDeskNameCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public ServiceDeskNameCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Project.PROP_SERVICE_DESK_NAME);
		var predicate = builder.and(
				builder.like(builder.lower(attribute), value.toLowerCase().replace("*", "%")), 
				builder.not(builder.like(attribute, Project.NULL_SERVICE_DESK_PREFIX + "%")));
		if (operator == ProjectQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Project project) {
		var matches = project.getServiceDeskName() != null && WildcardUtils.matchString(value.toLowerCase(), project.getServiceDeskName().toLowerCase());
		if (operator == ProjectQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_NAME) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
