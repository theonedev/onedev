package io.onedev.server.job.match;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import static io.onedev.server.model.Build.NAME_PROJECT;

public class ProjectCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private String projectPath;
	
	public ProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public boolean matches(JobMatchContext context) {
		return WildcardUtils.matchPath(projectPath, context.getProject().getPath());
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
		return quote(NAME_PROJECT) + " " + JobMatch.getRuleName(JobMatchLexer.Is) + " " + quote(projectPath);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
