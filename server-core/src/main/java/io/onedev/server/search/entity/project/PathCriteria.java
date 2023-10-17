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
	
	public PathCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(builder, from, value);
	}

	@Override
	public boolean matches(Project project) {
		return WildcardUtils.matchPath(value.toLowerCase(), project.getPath().toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_PATH) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " 
				+ Criteria.quote(value);
	}

}
