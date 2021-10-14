package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class NameCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public NameCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		Path<String> attribute = root.get(Project.PROP_NAME);
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
