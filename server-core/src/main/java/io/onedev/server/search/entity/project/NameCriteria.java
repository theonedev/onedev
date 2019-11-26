package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.ProjectConstants;

public class NameCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public NameCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder, User user) {
		Path<String> attribute = root.get(ProjectConstants.ATTR_NAME);
		String normalized = value.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Project project, User user) {
		String name = project.getName();
		return name != null && WildcardUtils.matchString(value.toLowerCase(), name.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return ProjectQuery.quote(ProjectConstants.FIELD_NAME) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " 
				+ ProjectQuery.quote(value);
	}

}
