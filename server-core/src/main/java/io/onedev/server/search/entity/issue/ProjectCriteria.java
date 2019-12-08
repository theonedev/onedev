package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.query.IssueQueryConstants;
import io.onedev.server.util.query.ProjectQueryConstants;

public class ProjectCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String projectName;

	public ProjectCriteria(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder) {
		Path<String> attribute = root
				.join(IssueQueryConstants.ATTR_PROJECT, JoinType.INNER)
				.get(ProjectQueryConstants.ATTR_NAME);
		String normalized = projectName.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(Issue issue) {
		return WildcardUtils.matchString(projectName.toLowerCase(), 
				issue.getProject().getName().toLowerCase());
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueQueryConstants.FIELD_PROJECT) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ IssueQuery.quote(projectName);
	}

}
