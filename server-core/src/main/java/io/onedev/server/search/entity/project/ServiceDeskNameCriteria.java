package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ServiceDeskNameCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ServiceDeskNameCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Project.PROP_SERVICE_DESK_NAME);
		return builder.and(
				builder.like(builder.lower(attribute), value.replace("*", "%")), 
				builder.not(builder.like(attribute, Project.NULL_SERVICE_DESK_PREFIX + "%")));
	}

	@Override
	public boolean matches(Project project) {
		return project.getServiceDeskName() != null && WildcardUtils.matchString(value, project.getServiceDeskName());
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_NAME) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " 
				+ Criteria.quote(value);
	}

}
