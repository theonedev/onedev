package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ChildrenOfCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final String parentPath;
	
	public ChildrenOfCriteria(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getParentPath() {
		return parentPath;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Subquery<Project> parentQuery = query.subquery(Project.class);
		Root<Project> parentRoot = parentQuery.from(Project.class);
		parentQuery.select(parentRoot);

		ProjectService manager = OneDev.getInstance(ProjectService.class);
		return builder.exists(parentQuery.where(
				builder.equal(from.get(Project.PROP_PARENT), parentRoot), 
				manager.getPathMatchPredicate(builder, parentRoot, parentPath)));
	}

	@Override
	public boolean matches(Project project) {
		if (project.getParent() != null)
			return WildcardUtils.matchPath(parentPath, project.getParent().getPath());
		else
			return false;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.ChildrenOf) + " " + Criteria.quote(parentPath);
	}

}
