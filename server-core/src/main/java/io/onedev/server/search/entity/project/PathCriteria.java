package io.onedev.server.search.entity.project;

import javax.persistence.criteria.*;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class PathCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public PathCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		var predicate = OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(builder, from, value);
		if (operator == ProjectQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Project project) {
		var matches = WildcardUtils.matchPath(value.toLowerCase(), project.getPath().toLowerCase());
		if (operator == ProjectQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_PATH) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(value);
	}

}
