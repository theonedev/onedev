package io.onedev.server.search.entity.project;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class ServiceDeskEmailAddressCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public ServiceDeskEmailAddressCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Project.PROP_SERVICE_DESK_EMAIL_ADDRESS);
		var predicate = builder.like(builder.lower(attribute), value.toLowerCase().replace("*", "%"));
		if (operator == ProjectQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Project project) {
		var matches = project.getServiceDeskEmailAddress() != null && WildcardUtils.matchString(value.toLowerCase(), project.getServiceDeskEmailAddress().toLowerCase());
		if (operator == ProjectQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_SERVICE_DESK_EMAIL_ADDRESS) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
