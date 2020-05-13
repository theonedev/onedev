package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;

public class SourceProjectCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String projectName;
	
	public SourceProjectCriteria(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<String> attribute = root
				.join(PullRequest.PROP_SOURCE_PROJECT, JoinType.INNER)
				.get(Project.PROP_NAME);
		String normalized = projectName.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(PullRequest request) {
		Project project = request.getSourceProject();
		if (project != null) {
			return WildcardUtils.matchString(projectName.toLowerCase(), 
					project.getName().toLowerCase());
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_SOURCE_PROJECT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(projectName);
	}

}
