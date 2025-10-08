package io.onedev.server.job.match;

import static io.onedev.server.model.Build.NAME_PROJECT;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ProjectCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	private final int operator;
	
	public ProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public boolean matches(JobMatchContext context) {
		var matches = WildcardUtils.matchPath(projectPath, context.getProject().getPath());
		if (operator == JobMatchLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public void onMoveProject(String oldPath, String newPath) {
		projectPath = PathUtils.substituteSelfOrAncestor(projectPath, oldPath, newPath);
	}

	@Override
	public boolean isUsingProject(String projectPath) {
		return PathUtils.isSelfOrAncestor(projectPath, this.projectPath);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_PROJECT) + " " 
				+ JobMatch.getRuleName(operator) + " " 
				+ quote(projectPath);
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
