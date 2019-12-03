package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.ProjectQueryConstants;

public class DescriptionCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder, User user) {
		Expression<String> attribute = root.get(ProjectQueryConstants.ATTR_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(Project project, User user) {
		String description = project.getDescription();
		return description != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", description);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return ProjectQuery.quote(ProjectQueryConstants.FIELD_DESCRIPTION) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Contains) + " " 
				+ ProjectQuery.quote(value);
	}

}
