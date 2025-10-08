package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class DescriptionCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Expression<String> attribute = from.get(Project.PROP_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(Project project) {
		String description = project.getDescription();
		return description != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", description);
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_DESCRIPTION) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Contains) + " " 
				+ Criteria.quote(value);
	}

}
