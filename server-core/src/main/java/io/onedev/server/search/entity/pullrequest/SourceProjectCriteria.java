package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class SourceProjectCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String projectPath;
	
	private final int operator;
	
	public SourceProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		var predicate = OneDev.getInstance(ProjectService.class).getPathMatchPredicate(builder,
				from.join(PullRequest.PROP_SOURCE_PROJECT, JoinType.INNER), projectPath);
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		Project project = request.getSourceProject();
		boolean matches;
		if (project != null) 
			matches = WildcardUtils.matchPath(projectPath, project.getPath());
		else 
			matches = false;
		
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_SOURCE_PROJECT) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(projectPath);
	}

}
