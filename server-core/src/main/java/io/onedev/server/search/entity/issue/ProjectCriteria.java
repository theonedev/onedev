package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String projectPath;

	public ProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(
				builder, from.join(Issue.PROP_PROJECT, JoinType.INNER), projectPath);
	}

	@Override
	public boolean matches(Issue issue) {
		return WildcardUtils.matchPath(projectPath, issue.getProject().getPath());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_PROJECT) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(projectPath);
	}

}
