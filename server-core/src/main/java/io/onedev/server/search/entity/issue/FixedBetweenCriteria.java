package io.onedev.server.search.entity.issue;

import static io.onedev.server.search.entity.issue.IssueQuery.getRuleName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.criteria.Criteria;

public class FixedBetweenCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final int firstType;
	
	private final String firstValue;
	
	private final int secondType;
	
	private final String secondValue;
	
	private transient ProjectAndCommitIds projectAndCommitIds;
	
	public FixedBetweenCriteria(@Nullable Project project, int firstType, String firstValue, 
			int secondType, String secondValue) {
		this.project = project;
		this.firstType = firstType;
		this.firstValue = firstValue;
		this.secondType = secondType;
		this.secondValue = secondValue;
	}
	
	private ProjectAndCommitIds getProjectAndCommitIds() {
		if (projectAndCommitIds == null) {
			ProjectScopedCommit first = getCommitId(project, firstType, firstValue);
			ProjectScopedCommit second = getCommitId(project, secondType, secondValue);
			if (first.getProject().equals(second.getProject())) { 
				projectAndCommitIds = new ProjectAndCommitIds(
						first.getProject(), first.getCommitId(), second.getCommitId());
			} else {
				throw new ExplicitException("'" + getRuleName(IssueQueryLexer.FixedBetween) 
					+ "' should be used for same projects");
			}
		}
		return projectAndCommitIds;
	}
	
	private static ProjectScopedCommit getCommitId(@Nullable Project project, int type, String value) {
		if (type == IssueQueryLexer.Build) {
			Build build = EntityQuery.getBuild(project, value);
			return new ProjectScopedCommit(build.getProject(), build.getCommitId());
		} else {
			return EntityQuery.getCommitId(project, value);
		}
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Set<Long> fixedIssueIds = new HashSet<>();
		
		Project project = getProjectAndCommitIds().project;
		ObjectId firstCommitId = getProjectAndCommitIds().firstCommitId;
		ObjectId secondCommitId = getProjectAndCommitIds().secondCommitId;
		
		ObjectId mergeBaseId = getGitService().getMergeBase(project, firstCommitId, project, secondCommitId);
		if (mergeBaseId != null) {
			Collection<ObjectId> startCommitIds = Lists.newArrayList(secondCommitId, firstCommitId);
			Collection<ObjectId> uninterestingCommitIds = Lists.newArrayList(mergeBaseId);
			for (RevCommit commit: getGitService().getReachableCommits(project, startCommitIds, uninterestingCommitIds))
				fixedIssueIds.addAll(project.parseFixedIssueIds(commit.getFullMessage()));
		}

		Predicate issuePredicate;
		if (!fixedIssueIds.isEmpty()) 
			issuePredicate = from.get(Issue.PROP_ID).in(fixedIssueIds);
		else 
			issuePredicate = builder.disjunction();
		return builder.and(
				builder.equal(from.get(Issue.PROP_PROJECT), project), 
				issuePredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		Project project = getProjectAndCommitIds().project;
		ObjectId firstCommitId = getProjectAndCommitIds().firstCommitId;
		ObjectId secondCommitId = getProjectAndCommitIds().secondCommitId;
		if (project.equals(issue.getProject())) {
			ObjectId mergeBaseId = getGitService().getMergeBase(project, firstCommitId, project, secondCommitId);
			if (mergeBaseId != null) {
				Collection<ObjectId> startCommitIds = Lists.newArrayList(secondCommitId, firstCommitId);
				Collection<ObjectId> uninterestingCommitIds = Lists.newArrayList(mergeBaseId);
				for (RevCommit commit: getGitService().getReachableCommits(project, startCommitIds, uninterestingCommitIds)) {
					if (project.parseFixedIssueIds(commit.getFullMessage()).contains(issue.getId()))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(IssueQueryLexer.FixedBetween) + " " 
				+ getRuleName(firstType) + " " + quote(firstValue) + " " 
				+ getRuleName(IssueQueryLexer.And) + " " 
				+ getRuleName(secondType) + " " + quote(secondValue);
	}

	private static class ProjectAndCommitIds {
		
		final Project project;
		
		final ObjectId firstCommitId;
		
		final ObjectId secondCommitId;
		
		ProjectAndCommitIds(Project project, ObjectId firstCommitId, ObjectId secondCommitId) {
			this.project = project;
			this.firstCommitId = firstCommitId;
			this.secondCommitId = secondCommitId;
		}
		
	}
}
