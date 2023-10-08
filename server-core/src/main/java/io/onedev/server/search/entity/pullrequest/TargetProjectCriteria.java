package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class TargetProjectCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;
	
	private final String projectPath;

	public TargetProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Project> project = from.join(PullRequest.PROP_TARGET_PROJECT, JoinType.INNER);
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(builder, project, projectPath);
	}

	@Override
	public boolean matches(PullRequest request) {
		return WildcardUtils.matchPath(projectPath, request.getTargetProject().getPath());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_TARGET_PROJECT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(projectPath);
	}

}
