package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class NameCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public NameCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Project.PROP_NAME);
		return builder.like(builder.lower(attribute), value.replace("*", "%"));
	}

	@Override
	public boolean matches(Project project) {
		return WildcardUtils.matchString(value, project.getName());
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_NAME) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " 
				+ Criteria.quote(value);
	}

}
