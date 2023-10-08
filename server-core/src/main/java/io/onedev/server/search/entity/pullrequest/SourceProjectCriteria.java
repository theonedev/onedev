package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class SourceProjectCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String projectPath;
	
	public SourceProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(builder, 
				from.join(PullRequest.PROP_SOURCE_PROJECT, JoinType.INNER), projectPath);
	}

	@Override
	public boolean matches(PullRequest request) {
		Project project = request.getSourceProject();
		if (project != null) 
			return WildcardUtils.matchPath(projectPath, project.getPath());
		else 
			return false;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_SOURCE_PROJECT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(projectPath);
	}

}
